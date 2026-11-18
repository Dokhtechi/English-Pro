package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class SpeechModelSpec(
    val id: String,
    val name: String,
    val version: String,
    val sizeBytes: Long,
    val languageAccent: String,
    val license: String,
    val downloadUrl: String,
    val notes: String,
    val isInstalled: Boolean = false,
    val isVerified: Boolean = false,
    val localFolder: String = ""
)

data class SpeechEngineDiagnostics(
    val androidStandardAvailable: Boolean = false,
    val androidOnDeviceAvailable: Boolean = false,
    val availableRecognitionServices: List<String> = emptyList(),
    val voskUsModelInstalled: Boolean = false,
    val voskUkModelInstalled: Boolean = false,
    val activeProviderName: String = "Manual Fallback"
)

interface SpeechRecognitionProvider {
    val providerId: String
    val displayName: String
    fun isAvailable(): Boolean
    fun startListening(localeTag: String, preferOffline: Boolean, onResult: (String) -> Unit, onError: (String) -> Unit)
    fun stopListening()
}

/**
 * Interchangeable speech-processing manager supporting:
 * 1. Continuous pause-tolerant Android SpeechRecognizer (auto-continues across thinking pauses)
 * 2. Vosk Offline Model Management (vosk-model-small-en-us-0.15 ~40MB & optional UK model)
 * 3. Manual Transcription Fallback (always available, preserves audio recordings)
 */
class SpeechRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _diagnostics = MutableStateFlow(inspectCapabilities())
    val diagnostics: StateFlow<SpeechEngineDiagnostics> = _diagnostics.asStateFlow()

    private var sessionActive = false
    private var accumulatedSegments = mutableListOf<String>()
    private var latestPartial = ""
    private var consecutivePauseCycles = 0
    private var busyRetryCount = 0
    private var activeLocaleTag = "en-US"
    private var activePreferOffline = false
    private var activeProviderId = "ANDROID_RECOGNIZER"

    private var onLiveTranscriptCallback: ((String, String) -> Unit)? = null
    private var onSessionFatalErrorCallback: ((String) -> Unit)? = null
    private var onSessionAutoFinishedCallback: ((String, String) -> Unit)? = null

    private var pendingRestartRunnable: Runnable? = null

    fun modelsDir(): File {
        val dir = File(context.filesDir, "speech_models")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getAvailableModelSpecs(): List<SpeechModelSpec> {
        val usDir = File(modelsDir(), "vosk-model-small-en-us-0.15")
        val ukDir = File(modelsDir(), "vosk-model-small-en-gb")
        return listOf(
            SpeechModelSpec(
                id = "vosk_en_us_small",
                name = "vosk-model-small-en-us-0.15",
                version = "0.15",
                sizeBytes = 41_943_040L, // ~40 MB
                languageAccent = "English (American - US)",
                license = "Apache License 2.0",
                downloadUrl = "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip",
                notes = "Primary offline US English model (~40 MB). Evaluate accuracy on your device; American model does not recognize British accents equally well.",
                isInstalled = usDir.exists() && verifyModelDir(usDir),
                isVerified = verifyModelDir(usDir),
                localFolder = usDir.absolutePath
            ),
            SpeechModelSpec(
                id = "vosk_en_gb_small",
                name = "vosk-model-small-en-gb",
                version = "0.15-uk",
                sizeBytes = 42_500_000L, // ~40.5 MB
                languageAccent = "English (British - UK Optional)",
                license = "Apache License 2.0",
                downloadUrl = "https://alphacephei.com/vosk/models",
                notes = "Optional British English offline candidate. Compatibility depends on device storage & Vosk native runtime.",
                isInstalled = ukDir.exists() && verifyModelDir(ukDir),
                isVerified = verifyModelDir(ukDir),
                localFolder = ukDir.absolutePath
            )
        )
    }

    fun verifyModelDir(dir: File): Boolean {
        if (!dir.exists() || !dir.isDirectory) return false
        val marker = File(dir, "MODEL_VERIFIED.json")
        val am = File(dir, "am/final.mdl")
        return marker.exists() || am.exists()
    }

    fun removeModel(modelId: String): Boolean {
        val folderName = when (modelId) {
            "vosk_en_us_small" -> "vosk-model-small-en-us-0.15"
            "vosk_en_gb_small" -> "vosk-model-small-en-gb"
            else -> return false
        }
        val target = File(modelsDir(), folderName)
        val deleted = target.deleteRecursively()
        refreshDiagnostics()
        return deleted
    }

    fun inspectCapabilities(): SpeechEngineDiagnostics {
        val standardAvail = SpeechRecognizer.isRecognitionAvailable(context)
        val onDeviceAvail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
            } catch (_: Exception) {
                false
            }
        } else {
            false
        }
        val services = try {
            val intent = Intent("android.speech.RecognitionService")
            context.packageManager.queryIntentServices(intent, 0)
                .mapNotNull { it.serviceInfo?.packageName }
                .distinct()
        } catch (_: Exception) {
            emptyList()
        }

        val usInstalled = verifyModelDir(File(modelsDir(), "vosk-model-small-en-us-0.15"))
        val ukInstalled = verifyModelDir(File(modelsDir(), "vosk-model-small-en-gb"))

        val activeName = when {
            onDeviceAvail -> "Android On-Device SpeechRecognizer"
            standardAvail -> "Android SpeechRecognizer (Network/Hybrid) + Manual Fallback"
            usInstalled -> "Vosk Model Files Installed (Manual Fallback active until JNI decoder enabled)"
            else -> "Local Recording + Manual Transcription Fallback"
        }

        return SpeechEngineDiagnostics(
            androidStandardAvailable = standardAvail,
            androidOnDeviceAvailable = onDeviceAvail,
            availableRecognitionServices = services,
            voskUsModelInstalled = usInstalled,
            voskUkModelInstalled = ukInstalled,
            activeProviderName = activeName
        )
    }

    fun refreshDiagnostics() {
        _diagnostics.value = inspectCapabilities()
    }

    /**
     * Starts pause-tolerant continuous speech recognition.
     * Brief silences/pauses or code 7/6 cycles seamlessly resume until [finishContinuousSession]
     * or [stopListening] is called by the user.
     */
    fun startContinuousRecognition(
        localeTag: String = "en-US",
        preferOffline: Boolean = false,
        onLiveTranscript: (fullText: String, providerId: String) -> Unit,
        onAutoCompleted: (fullText: String, providerId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val diag = inspectCapabilities()
        if (!diag.androidStandardAvailable && !diag.androidOnDeviceAvailable) {
            onError("No system speech recognizer service found. Recording locally; enter or edit transcript manually.")
            return
        }

        stopListening()
        sessionActive = true
        accumulatedSegments.clear()
        latestPartial = ""
        consecutivePauseCycles = 0
        busyRetryCount = 0
        activeLocaleTag = localeTag
        activePreferOffline = preferOffline
        activeProviderId = if (preferOffline && diag.androidOnDeviceAvailable) {
            "ANDROID_ON_DEVICE"
        } else {
            "ANDROID_RECOGNIZER"
        }
        onLiveTranscriptCallback = onLiveTranscript
        onSessionAutoFinishedCallback = onAutoCompleted
        onSessionFatalErrorCallback = onError

        _isListening.value = true
        _partialText.value = ""
        launchRecognizerPass(diag)
    }

    /**
     * Backward-compatible single-shot wrapper.
     */
    fun startSpeechRecognition(
        localeTag: String = "en-US",
        preferOffline: Boolean = true,
        onFinalResult: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        startContinuousRecognition(
            localeTag = localeTag,
            preferOffline = preferOffline,
            onLiveTranscript = { _, _ -> },
            onAutoCompleted = onFinalResult,
            onError = onError
        )
    }

    fun finishContinuousSession(): Pair<String, String> {
        val text = buildCombinedTranscript()
        val provider = activeProviderId
        stopListening()
        return text to provider
    }

    private fun buildCombinedTranscript(): String {
        val parts = accumulatedSegments.toMutableList()
        if (latestPartial.isNotBlank()) {
            parts.add(latestPartial.trim())
        }
        return parts.joinToString(" ").replace(Regex("\\s+"), " ").trim()
    }

    private fun launchRecognizerPass(diag: SpeechEngineDiagnostics = inspectCapabilities()) {
        mainHandler.post {
            if (!sessionActive) return@post
            try {
                destroyCurrentRecognizerOnly()
                val recognizer = if (
                    activePreferOffline &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    diag.androidOnDeviceAvailable
                ) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                } else {
                    SpeechRecognizer.createSpeechRecognizer(context)
                }
                speechRecognizer = recognizer

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        if (sessionActive) _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        consecutivePauseCycles = 0
                    }

                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}

                    override fun onPartialResults(partialResults: Bundle?) {
                        if (!sessionActive) return
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                            .trim()
                        if (partial.isNotBlank()) {
                            consecutivePauseCycles = 0
                            latestPartial = partial
                            _partialText.value = partial
                            onLiveTranscriptCallback?.invoke(buildCombinedTranscript(), activeProviderId)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        if (!sessionActive) return
                        val best = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                            .trim()
                        if (best.isNotBlank()) {
                            accumulatedSegments.add(best)
                            latestPartial = ""
                            _partialText.value = ""
                            consecutivePauseCycles = 0
                            busyRetryCount = 0
                            onLiveTranscriptCallback?.invoke(buildCombinedTranscript(), activeProviderId)
                        }
                        scheduleNextRecognizerPass(delayMs = 120L)
                    }

                    override fun onError(error: Int) {
                        if (!sessionActive) return

                        // Preserve any partial phrase already heard before the pause
                        if (latestPartial.isNotBlank()) {
                            accumulatedSegments.add(latestPartial.trim())
                            latestPartial = ""
                            _partialText.value = ""
                            onLiveTranscriptCallback?.invoke(buildCombinedTranscript(), activeProviderId)
                        }

                        when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH,
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                            SpeechRecognizer.ERROR_CLIENT -> {
                                consecutivePauseCycles++
                                if (consecutivePauseCycles <= 15) {
                                    scheduleNextRecognizerPass(delayMs = 180L)
                                } else {
                                    val finalText = buildCombinedTranscript()
                                    val cb = onSessionAutoFinishedCallback
                                    stopListening()
                                    if (finalText.isNotBlank()) {
                                        cb?.invoke(finalText, activeProviderId)
                                    }
                                }
                            }

                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                                busyRetryCount++
                                if (busyRetryCount <= 3) {
                                    scheduleNextRecognizerPass(delayMs = 350L)
                                } else {
                                    val cb = onSessionFatalErrorCallback
                                    stopListening()
                                    cb?.invoke("Speech recognizer busy; please tap the microphone again or type your answer.")
                                }
                            }

                            SpeechRecognizer.ERROR_NETWORK,
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                                val cb = onSessionFatalErrorCallback
                                stopListening()
                                cb?.invoke("Speech recognition needs internet on this device or an installed offline pack. Recording locally instead.")
                            }

                            else -> {
                                val finalText = buildCombinedTranscript()
                                if (finalText.isNotBlank()) {
                                    // Keep what was spoken instead of discarding it as an error
                                    val cb = onSessionAutoFinishedCallback
                                    stopListening()
                                    cb?.invoke(finalText, activeProviderId)
                                } else {
                                    val cb = onSessionFatalErrorCallback
                                    stopListening()
                                    cb?.invoke("Speech recognition unavailable (code $error); switched to local audio/manual mode.")
                                }
                            }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, activeLocaleTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    // Give generous pause/silence tolerance so thinking pauses do not cut off the learner
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 6000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 4500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 15000L)
                    if (activePreferOffline) {
                        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    }
                }
                recognizer.startListening(intent)
            } catch (e: Exception) {
                val cb = onSessionFatalErrorCallback
                stopListening()
                cb?.invoke("Failed to start speech recognizer: ${e.localizedMessage}. Switched to local mode.")
            }
        }
    }

    private fun scheduleNextRecognizerPass(delayMs: Long) {
        pendingRestartRunnable?.let { mainHandler.removeCallbacks(it) }
        val runnable = Runnable {
            if (sessionActive) {
                launchRecognizerPass()
            }
        }
        pendingRestartRunnable = runnable
        mainHandler.postDelayed(runnable, delayMs)
    }

    private fun destroyCurrentRecognizerOnly() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }
        speechRecognizer = null
    }

    fun stopListening() {
        sessionActive = false
        pendingRestartRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingRestartRunnable = null
        onLiveTranscriptCallback = null
        onSessionAutoFinishedCallback = null
        onSessionFatalErrorCallback = null
        _isListening.value = false
        _partialText.value = ""
        latestPartial = ""

        if (Looper.myLooper() == Looper.getMainLooper()) {
            destroyCurrentRecognizerOnly()
        } else {
            mainHandler.post { destroyCurrentRecognizerOnly() }
        }
    }
}
