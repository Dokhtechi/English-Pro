package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

private const val TAG = "TextToSpeechManager"

data class TtsStatus(
    val isInitialized: Boolean = false,
    val usAvailable: Boolean = false,
    val usOfflineAvailable: Boolean = false,
    val ukAvailable: Boolean = false,
    val ukOfflineAvailable: Boolean = false,
    val installedEnglishOfflineVoices: List<String> = emptyList(),
    val activeAccent: String = "US",
    val activeVoiceGender: String = "FEMALE",
    val distinctMaleVoiceAvailable: Boolean = false,
    val distinctFemaleVoiceAvailable: Boolean = false,
    val errorMessage: String? = null
)

object VoiceGenderSelector {
    private val femaleTokens = listOf(
        "female", "-f-", "_f_", "sfg", "tpf", "tpc", "iob", "gba", "gbc", "fis", "sfh",
        "samantha", "victoria", "karen", "serena", "kate", "allison", "ava", "emma", "amy"
    )
    private val maleTokens = listOf(
        "male", "-m-", "_m_", "iol", "iom", "tpd", "rjs", "gbd", "gbb",
        "daniel", "alex", "oliver", "arthur", "ryan", "brian", "aaron", "matthew"
    )

    /**
     * Classifies a voice name string as "MALE", "FEMALE", or "NEUTRAL".
     */
    fun classifyVoiceName(voiceName: String): String {
        val lower = voiceName.lowercase(Locale.US)
        if (femaleTokens.any { lower.contains(it) }) return "FEMALE"
        // Avoid matching "female" as "male"
        val withoutFemale = lower.replace("female", "")
        if (maleTokens.any { withoutFemale.contains(it) }) return "MALE"
        return "NEUTRAL"
    }

    fun selectBestVoice(
        voices: Collection<Voice>,
        targetLocale: Locale,
        voiceGender: String,
        isOnline: Boolean
    ): Pair<Voice?, Boolean> {
        val englishVoices = voices.filter { v ->
            v.locale.language.equals("en", ignoreCase = true) &&
                (isOnline || !v.isNetworkConnectionRequired)
        }
        val localeMatches = englishVoices.filter {
            it.locale.country.equals(targetLocale.country, ignoreCase = true)
        }.ifEmpty { englishVoices }

        val desired = if (voiceGender.uppercase(Locale.US) == "MALE") "MALE" else "FEMALE"
        val genderMatch = localeMatches.firstOrNull { classifyVoiceName(it.name) == desired }
            ?: englishVoices.firstOrNull { classifyVoiceName(it.name) == desired }

        if (genderMatch != null) {
            return genderMatch to true
        }

        val fallback = localeMatches.firstOrNull { !it.isNetworkConnectionRequired }
            ?: localeMatches.firstOrNull()
        return fallback to false
    }
}

class TextToSpeechManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var internationalToggle = false

    private val _status = MutableStateFlow(TtsStatus())
    val status: StateFlow<TtsStatus> = _status.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { result ->
                if (result == TextToSpeech.SUCCESS) {
                    refreshVoiceCapabilities()
                } else {
                    _status.value = _status.value.copy(
                        isInitialized = false,
                        errorMessage = "Android TTS initialization failed (code $result)"
                    )
                }
            }
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "TTS construction error", e)
            _status.value = TtsStatus(isInitialized = false, errorMessage = e.localizedMessage)
        }
    }

    fun refreshVoiceCapabilities() {
        val engine = tts ?: return
        try {
            val usRes = engine.isLanguageAvailable(Locale.US)
            val ukRes = engine.isLanguageAvailable(Locale.UK)
            val voices: Set<Voice> = engine.voices ?: emptySet()

            val offlineEnVoices = voices.filter { v ->
                v.locale.language.equals("en", ignoreCase = true) && !v.isNetworkConnectionRequired
            }
            val allEnVoices = voices.filter { it.locale.language.equals("en", ignoreCase = true) }
            val usOffline = offlineEnVoices.any { it.locale.country.equals("US", ignoreCase = true) }
            val ukOffline = offlineEnVoices.any { it.locale.country.equals("GB", ignoreCase = true) }
            val hasMale = allEnVoices.any { VoiceGenderSelector.classifyVoiceName(it.name) == "MALE" }
            val hasFemale = allEnVoices.any { VoiceGenderSelector.classifyVoiceName(it.name) == "FEMALE" }

            _status.value = TtsStatus(
                isInitialized = true,
                usAvailable = usRes >= TextToSpeech.LANG_AVAILABLE,
                usOfflineAvailable = usOffline,
                ukAvailable = ukRes >= TextToSpeech.LANG_AVAILABLE,
                ukOfflineAvailable = ukOffline,
                installedEnglishOfflineVoices = offlineEnVoices.map { "${it.locale.displayName} (${it.name})" }.distinct(),
                activeAccent = _status.value.activeAccent,
                activeVoiceGender = _status.value.activeVoiceGender,
                distinctMaleVoiceAvailable = hasMale,
                distinctFemaleVoiceAvailable = hasFemale
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed inspecting TTS voices", e)
        }
    }

    /**
     * Speaks text locally using verified offline voice when offline, or selected US/UK accent + Male/Female preference.
     * When the device lacks a dedicated Male/Female voice pack, applies natural pitch modulation as an acoustic fallback.
     */
    fun speak(
        text: String,
        accentPreference: String = "US", // US, UK, INTERNATIONAL
        speechRate: Float = 1.0f,
        isOnline: Boolean = true,
        voiceGender: String = "FEMALE" // FEMALE, MALE
    ): Boolean {
        val engine = tts
        if (engine == null || !_status.value.isInitialized) return false

        val resolvedAccent = when (accentPreference) {
            "UK" -> "UK"
            "INTERNATIONAL" -> {
                internationalToggle = !internationalToggle
                if (internationalToggle) "US" else "UK"
            }
            else -> "US"
        }
        val targetLocale = if (resolvedAccent == "UK") Locale.UK else Locale.US
        val resolvedGender = if (voiceGender.uppercase(Locale.US) == "MALE") "MALE" else "FEMALE"

        val voices = engine.voices ?: emptySet()
        val (selectedVoice, exactGenderMatched) = VoiceGenderSelector.selectBestVoice(
            voices = voices,
            targetLocale = targetLocale,
            voiceGender = resolvedGender,
            isOnline = isOnline
        )

        if (!isOnline && selectedVoice == null && _status.value.installedEnglishOfflineVoices.isEmpty()) {
            _status.value = _status.value.copy(
                errorMessage = "No offline English TTS voice installed on this device."
            )
            return false
        }

        if (selectedVoice != null) {
            engine.voice = selectedVoice
        } else {
            engine.language = targetLocale
        }

        // Apply natural pitch if exact gender voice package isn't installed on this device
        val pitch = when {
            exactGenderMatched -> 1.0f
            resolvedGender == "MALE" -> 0.82f
            else -> 1.06f
        }
        engine.setPitch(pitch)
        engine.setSpeechRate(speechRate.coerceIn(0.5f, 1.5f))
        _status.value = _status.value.copy(
            activeAccent = resolvedAccent,
            activeVoiceGender = resolvedGender,
            errorMessage = null
        )
        val utteranceId = UUID.randomUUID().toString()
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        return true
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
    }
}
