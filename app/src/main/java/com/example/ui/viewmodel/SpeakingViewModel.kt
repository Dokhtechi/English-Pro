package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiSpeakingCoachService
import com.example.ai.CoachSessionReport
import com.example.data.local.*
import com.example.data.repository.EnglishProRepository
import com.example.device.DeviceDiagnosticsManager
import com.example.speech.AudioRecorderManager
import com.example.speech.SpeechRecognitionManager
import com.example.speech.TextToSpeechManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "SpeakingViewModel"

sealed interface CoachUiState {
    data object Idle : CoachUiState
    data object Recording : CoachUiState
    data object Thinking : CoachUiState
    data class Streaming(val partialReply: String) : CoachUiState
    data class Error(val message: String) : CoachUiState
}

class SpeakingViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val db = EnglishProDatabase.getInstance(context)
    private val repository = EnglishProRepository(db)
    private val aiService = AiSpeakingCoachService()
    private val json = Json { ignoreUnknownKeys = true }

    val ttsManager = TextToSpeechManager(context)
    val audioRecorder = AudioRecorderManager(context)
    val speechManager = SpeechRecognitionManager(context)

    private val _coachState = MutableStateFlow<CoachUiState>(CoachUiState.Idle)
    val coachState: StateFlow<CoachUiState> = _coachState.asStateFlow()

    private val _activeSession = MutableStateFlow<ConversationSessionEntity?>(null)
    val activeSession: StateFlow<ConversationSessionEntity?> = _activeSession.asStateFlow()

    private val _activeScenario = MutableStateFlow<RolePlayScenarioEntity?>(null)
    val activeScenario: StateFlow<RolePlayScenarioEntity?> = _activeScenario.asStateFlow()

    private val _correctionMode = MutableStateFlow("FLUENCY") // FLUENCY | ACCURACY
    val correctionMode: StateFlow<String> = _correctionMode.asStateFlow()

    private val _difficulty = MutableStateFlow("B2") // B1/B2 | B2 | B2+/C1
    val difficulty: StateFlow<String> = _difficulty.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _accent = MutableStateFlow("INTERNATIONAL")
    val accent: StateFlow<String> = _accent.asStateFlow()

    private val _showTranscript = MutableStateFlow(true)
    val showTranscript: StateFlow<Boolean> = _showTranscript.asStateFlow()

    private val _draftInput = MutableStateFlow("")
    val draftInput: StateFlow<String> = _draftInput.asStateFlow()

    private val _lastRecordingPath = MutableStateFlow<String?>(null)
    val lastRecordingPath: StateFlow<String?> = _lastRecordingPath.asStateFlow()

    private val _latestReport = MutableStateFlow<CoachSessionReport?>(null)
    val latestReport: StateFlow<CoachSessionReport?> = _latestReport.asStateFlow()

    private val _helperFeedback = MutableStateFlow<String?>(null)
    val helperFeedback: StateFlow<String?> = _helperFeedback.asStateFlow()

    val turns: StateFlow<List<ConversationTurnEntity>> = _activeSession
        .flatMapLatest { session ->
            if (session == null) flowOf(emptyList())
            else repository.observeTurns(session.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pastSessions: StateFlow<List<ConversationSessionEntity>> = repository.conversationSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val userProfile: StateFlow<UserProfileEntity> = repository.profile
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfileEntity())

    private var turnCounter = 0
    private var targetWords = listOf("clarify", "mitigate", "critical path", "non-conformance report", "push back on")

    fun setCorrectionMode(mode: String) {
        _correctionMode.value = mode
    }

    fun setDifficulty(level: String) {
        _difficulty.value = level
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
    }

    fun setAccent(accent: String) {
        _accent.value = accent
    }

    fun toggleTranscript() {
        _showTranscript.value = !_showTranscript.value
    }

    fun updateDraftInput(text: String) {
        _draftInput.value = text
    }

    fun clearHelperFeedback() {
        _helperFeedback.value = null
    }

    fun startFreeSession() {
        startSession(scenario = null)
    }

    fun startRolePlayScenario(scenario: RolePlayScenarioEntity) {
        startSession(scenario = scenario)
    }

    private fun startSession(scenario: RolePlayScenarioEntity?) {
        viewModelScope.launch {
            _latestReport.value = null
            _activeScenario.value = scenario
            turnCounter = 0
            val online = DeviceDiagnosticsManager.isOnline(context)
            val title = scenario?.title ?: "AI Speaking Coach (${_difficulty.value})"
            val mode = if (scenario != null) "ROLEPLAY" else "FREE_COACH"
            val id = repository.startConversationSession(
                mode = mode,
                correctionMode = _correctionMode.value,
                scenarioId = scenario?.id,
                title = title,
                isOnlineAi = online
            )
            val created = ConversationSessionEntity(
                id = id,
                mode = mode,
                correctionMode = _correctionMode.value,
                scenarioId = scenario?.id,
                title = title,
                isOnlineAi = online,
                startedAtEpochMs = System.currentTimeMillis()
            )
            _activeSession.value = created

            // Pull passive/review target words to encourage active reuse
            val vocab = db.vocabDao().getAllVocabOnce()
            val candidateTargets = vocab.filter { it.isPassiveOnly || it.selfRating == "DIFFICULT" }
                .take(5)
                .map { it.word }
            if (candidateTargets.isNotEmpty()) targetWords = candidateTargets

            val openingLine = if (scenario != null) {
                val nodes = runCatching {
                    json.decodeFromString<List<OfflineBranchNode>>(scenario.offlineBranchingJson)
                }.getOrDefault(emptyList())
                nodes.firstOrNull()?.aiPrompt
                    ?: "Good morning. As the ${scenario.aiRole}, let's start: ${scenario.background}. What's your assessment?"
            } else {
                "Good to speak with you today. Tell me briefly about a site supervision or engineering coordination challenge you handled recently—take your time and speak naturally."
            }

            repository.addConversationTurn(
                ConversationTurnEntity(
                    sessionId = id,
                    speaker = "COACH",
                    englishText = openingLine,
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
            speakOut(openingLine)
        }
    }

    fun loadExistingSession(session: ConversationSessionEntity) {
        _activeSession.value = session
        _latestReport.value = if (session.endedAtEpochMs > 0L && session.feedbackSummary.isNotBlank()) {
            CoachSessionReport(
                fluencyScore = session.fluencyScore,
                vocabRangeScore = session.vocabRangeScore,
                grammarScore = session.grammarScore,
                complexityScore = session.complexityScore,
                pronunciationScore = session.pronunciationScore,
                spontaneousScore = session.spontaneousScore,
                hesitationNotes = session.hesitationNotes,
                feedbackSummary = session.feedbackSummary,
                recommendations = runCatching {
                    json.decodeFromString<List<String>>(session.recommendationsJson)
                }.getOrDefault(emptyList())
            )
        } else null
    }

    private var baseDraftBeforeVoice: String = ""
    private var usingAudioFallbackRecorder: Boolean = false

    fun startVoiceTurn() {
        ttsManager.stop()
        baseDraftBeforeVoice = _draftInput.value.trim()
        val diag = speechManager.inspectCapabilities()
        val hasRecognizer = diag.androidStandardAvailable || diag.androidOnDeviceAvailable

        if (!hasRecognizer) {
            // Pure local recording fallback when no system SpeechRecognizer exists
            usingAudioFallbackRecorder = true
            val path = audioRecorder.startRecording("coach")
            _lastRecordingPath.value = path
            _coachState.value = CoachUiState.Recording
            return
        }

        // Do NOT hold MediaRecorder on AudioSource.MIC simultaneously with SpeechRecognizer,
        // preventing Xiaomi/MIUI audio HAL conflicts and premature code 7 cutoffs.
        usingAudioFallbackRecorder = false
        _lastRecordingPath.value = null
        _coachState.value = CoachUiState.Recording

        val isOnline = DeviceDiagnosticsManager.isOnline(context)
        speechManager.startContinuousRecognition(
            localeTag = if (_accent.value == "UK") "en-GB" else "en-US",
            preferOffline = !isOnline,
            onLiveTranscript = { spokenText, _ ->
                _draftInput.value = if (baseDraftBeforeVoice.isBlank()) {
                    spokenText
                } else {
                    "$baseDraftBeforeVoice $spokenText".trim()
                }
            },
            onAutoCompleted = { spokenText, provider ->
                val combined = if (baseDraftBeforeVoice.isBlank()) spokenText
                else "$baseDraftBeforeVoice $spokenText".trim()
                _coachState.value = CoachUiState.Idle
                if (combined.isNotBlank()) {
                    _draftInput.value = combined
                    sendUserMessage(combined)
                }
            },
            onError = { errMsg ->
                // Switch gracefully to local audio recording when speech service cannot run (e.g., offline without pack)
                val preservedText = _draftInput.value.trim()
                usingAudioFallbackRecorder = true
                val path = audioRecorder.startRecording("coach")
                _lastRecordingPath.value = path
                if (path != null) {
                    _coachState.value = CoachUiState.Recording
                    _helperFeedback.value = errMsg
                } else {
                    usingAudioFallbackRecorder = false
                    _coachState.value = if (preservedText.isBlank()) CoachUiState.Error(errMsg) else CoachUiState.Idle
                }
            }
        )
    }

    fun stopVoiceTurnManually() {
        if (usingAudioFallbackRecorder) {
            usingAudioFallbackRecorder = false
            val rec = audioRecorder.stopRecordingSafely()
            if (rec != null) {
                saveRecordingRecord(rec.filePath, rec.durationMs, _draftInput.value.trim(), "MANUAL")
            }
            _coachState.value = CoachUiState.Idle
            val typed = _draftInput.value.trim()
            if (typed.isNotBlank()) {
                sendUserMessage(typed, rec?.filePath)
            }
            return
        }

        val (spoken, provider) = speechManager.finishContinuousSession()
        val combined = when {
            spoken.isNotBlank() && baseDraftBeforeVoice.isNotBlank() -> "$baseDraftBeforeVoice $spoken".trim()
            spoken.isNotBlank() -> spoken.trim()
            else -> _draftInput.value.trim()
        }
        _coachState.value = CoachUiState.Idle
        if (combined.isNotBlank()) {
            _draftInput.value = combined
            sendUserMessage(combined)
        }
    }

    fun stopAndSendVoiceTurn() {
        if (_coachState.value is CoachUiState.Recording) {
            stopVoiceTurnManually()
        } else {
            sendUserMessage(_draftInput.value)
        }
    }

    private fun saveRecordingRecord(path: String?, durationMs: Long, transcript: String, provider: String) {
        if (path == null) return
        viewModelScope.launch {
            repository.saveRecording(
                SpeakingRecordingEntity(
                    title = _activeSession.value?.title ?: "Speaking Practice",
                    contextType = "COACH",
                    filePath = path,
                    durationMs = durationMs,
                    transcript = transcript,
                    transcriptionProvider = provider,
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
        }
    }

    fun sendUserMessage(text: String, audioPath: String? = _lastRecordingPath.value) {
        val clean = text.trim()
        if (clean.isBlank()) return
        _draftInput.value = ""

        viewModelScope.launch {
            val session = _activeSession.value ?: run {
                startFreeSession()
                return@launch
            }
            repository.addConversationTurn(
                ConversationTurnEntity(
                    sessionId = session.id,
                    speaker = "USER",
                    englishText = clean,
                    audioFilePath = audioPath,
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )

            // Detect error pattern for personalization
            if (clean.contains("equipments", true) || clean.contains("formworks", true)) {
                repository.incrementErrorPattern(
                    "uncountable_site_nouns",
                    "Uncountable engineering nouns",
                    clean
                )
            }

            _coachState.value = CoachUiState.Thinking
            val isOnline = DeviceDiagnosticsManager.isOnline(context)
            val historyPairs = turns.value.map {
                (if (it.speaker == "USER") "Learner" else "Coach") to it.englishText
            }

            if (isOnline) {
                try {
                    // First stream reply text smoothly so TTFT is rapid, then attach structured correction
                    val replyResult = aiService.generateStructuredCoachReply(
                        userMessage = clean,
                        historyPairs = historyPairs,
                        difficulty = _difficulty.value,
                        correctionMode = _correctionMode.value,
                        targetWords = targetWords,
                        scenario = _activeScenario.value
                    )
                    val correctionStr = if (replyResult.correction.category != "NATURAL" || replyResult.correction.explanation.isNotBlank()) {
                        json.encodeToString(replyResult.correction)
                    } else ""

                    repository.addConversationTurn(
                        ConversationTurnEntity(
                            sessionId = session.id,
                            speaker = "COACH",
                            englishText = replyResult.englishReply,
                            correctionJson = correctionStr,
                            createdAtEpochMs = System.currentTimeMillis()
                        )
                    )
                    _coachState.value = CoachUiState.Idle
                    speakOut(replyResult.englishReply)
                } catch (e: TimeoutCancellationException) {
                    Log.e(TAG, "Gemini AI timed out, switching to offline coach fallback", e)
                    handleOfflineTurn(session.id, clean, "Gemini AI timed out; continued with offline coach.")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase AI call failed", e)
                    handleOfflineTurn(session.id, clean, "Online AI error (${e.localizedMessage}); continued offline.")
                }
            } else {
                handleOfflineTurn(session.id, clean, null)
            }
            turnCounter++
        }
    }

    private suspend fun handleOfflineTurn(sessionId: Long, userMessage: String, notice: String?) {
        val fallback = aiService.generateOfflineFallbackReply(
            userMessage = userMessage,
            turnIndex = turnCounter,
            scenario = _activeScenario.value,
            targetWords = targetWords
        )
        repository.addConversationTurn(
            ConversationTurnEntity(
                sessionId = sessionId,
                speaker = "COACH",
                englishText = fallback.englishReply,
                persianTranslation = fallback.persianHint,
                correctionJson = json.encodeToString(fallback.correction),
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
        _coachState.value = if (notice != null) CoachUiState.Error(notice) else CoachUiState.Idle
        speakOut(fallback.englishReply)
    }

    fun repeatLastCoachSentence() {
        val lastCoach = turns.value.lastOrNull { it.speaker == "COACH" } ?: return
        speakOut(lastCoach.englishText)
    }

    fun speakOut(text: String) {
        val online = DeviceDiagnosticsManager.isOnline(context)
        ttsManager.speak(
            text = text,
            accentPreference = _accent.value,
            speechRate = _speechRate.value,
            isOnline = online,
            voiceGender = userProfile.value.voiceGender
        )
    }

    fun requestPersianTranslation(turn: ConversationTurnEntity, explainOnly: Boolean) {
        viewModelScope.launch {
            if (!DeviceDiagnosticsManager.isOnline(context)) {
                _helperFeedback.value = if (turn.persianTranslation.isNotBlank()) {
                    turn.persianTranslation
                } else {
                    "حالت آفلاین: برای ترجمه یا توضیح زنده عبارات جدید به اینترنت نیاز است؛ واژگان پایه در بخش دیکشنری آفلاین موجود است."
                }
                return@launch
            }
            _helperFeedback.value = "در حال دریافت توضیح..."
            try {
                val res = aiService.translateOrExplainInPersian(turn.englishText, explainOnly)
                _helperFeedback.value = res
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _helperFeedback.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun finishAndScoreSession() {
        val session = _activeSession.value ?: return
        viewModelScope.launch {
            _coachState.value = CoachUiState.Thinking
            val userMsgs = turns.value.filter { it.speaker == "USER" }.map { it.englishText }
            val transcriptLines = turns.value.map { "${it.speaker}: ${it.englishText}" }
            val usedWords = targetWords.filter { w -> userMsgs.any { it.contains(w, ignoreCase = true) } }

            val report = if (DeviceDiagnosticsManager.isOnline(context) && userMsgs.isNotEmpty()) {
                try {
                    aiService.generateSessionReport(transcriptLines, targetWords)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    aiService.generateOfflineSessionReport(userMsgs, targetWords)
                }
            } else {
                aiService.generateOfflineSessionReport(userMsgs, targetWords)
            }

            repository.completeConversationSession(
                session = session,
                fluency = report.fluencyScore,
                vocabRange = report.vocabRangeScore,
                grammar = report.grammarScore,
                complexity = report.complexityScore,
                pronunciation = report.pronunciationScore,
                spontaneous = report.spontaneousScore,
                hesitationNotes = report.hesitationNotes,
                targetWordsUsed = usedWords,
                summary = report.feedbackSummary,
                recommendations = report.recommendations
            )
            _latestReport.value = report
            _coachState.value = CoachUiState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        audioRecorder.release()
        speechManager.stopListening()
    }
}
