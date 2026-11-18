package com.example.ui.viewmodel

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.EnglishProRepository
import com.example.device.DeviceDiagnosticsManager
import com.example.device.DeviceDiagnosticsReport
import com.example.device.FeatureMatrixItem
import com.example.domain.backup.BackupRepository
import com.example.domain.srs.FsrsScheduler
import com.example.download.PackageDownloadManager
import com.example.speech.AudioRecorderManager
import com.example.speech.SpeechModelSpec
import com.example.speech.SpeechRecognitionManager
import com.example.speech.TextToSpeechManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DailyPlanBreakdown(
    val totalTargetMinutes: Int,
    val srsMinutesTarget: Int,
    val newVocabMinutesTarget: Int,
    val speakingMinutesTarget: Int,
    val listeningMinutesTarget: Int,
    val professionalMinutesTarget: Int,
    val isNewVocabGatedByBacklog: Boolean
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    val db = EnglishProDatabase.getInstance(context)
    val repository = EnglishProRepository(db)
    val ttsManager = TextToSpeechManager(context)
    val speechManager = SpeechRecognitionManager(context)
    val audioRecorder = AudioRecorderManager(context)
    val packageManager = PackageDownloadManager(context)
    val backupRepository = BackupRepository(context, db)

    private val _isOnline = MutableStateFlow(DeviceDiagnosticsManager.isOnline(context))
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    val profile: StateFlow<UserProfileEntity> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileEntity())

    val allVocab: StateFlow<List<VocabWordEntity>> = repository.allVocab
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFsrsCards: StateFlow<List<FsrsCardEntity>> = repository.allFsrsCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueCards: StateFlow<List<FsrsCardEntity>> = repository.observeDueCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueCount: StateFlow<Int> = repository.observeDueCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeMasteredCount: StateFlow<Int> = repository.activeMasteredCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val passiveOnlyCount: StateFlow<Int> = repository.passiveOnlyCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val civilMasteredCount: StateFlow<Int> = repository.civilMasteredCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val packages: StateFlow<List<ContentPackageEntity>> = repository.packages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lessons: StateFlow<List<LessonModuleEntity>> = repository.lessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scenarios: StateFlow<List<RolePlayScenarioEntity>> = repository.scenarios
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assessments: StateFlow<List<AssessmentRecordEntity>> = repository.assessments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyLogs: StateFlow<List<StudyLogEntity>> = repository.studyLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val errorPatterns: StateFlow<List<GrammarErrorPatternEntity>> = repository.errorPatterns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviewLogs: StateFlow<List<ReviewLogEntity>> = repository.recentReviewLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversationSessions: StateFlow<List<ConversationSessionEntity>> = repository.conversationSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recordings: StateFlow<List<SpeakingRecordingEntity>> = repository.recordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedCefr = MutableStateFlow("ALL")
    val selectedCefr: StateFlow<String> = _selectedCefr.asStateFlow()

    val filteredVocab: StateFlow<List<VocabWordEntity>> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedCefr
    ) { q, cat, cefr -> Triple(q, cat, cefr) }
        .flatMapLatest { (q, cat, cefr) ->
            repository.searchVocab(q.trim(), cat, cefr)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyPlan: StateFlow<DailyPlanBreakdown> = combine(profile, dueCount) { p, due ->
        val total = p.dailyMinutesTarget
        val scale = total / 60.0
        val gated = FsrsScheduler.shouldGateNewVocabulary(due, p.overloadBacklogThreshold)
        DailyPlanBreakdown(
            totalTargetMinutes = total,
            srsMinutesTarget = (10 * scale).toInt().coerceAtLeast(5),
            newVocabMinutesTarget = if (gated) 0 else (10 * scale).toInt().coerceAtLeast(5),
            speakingMinutesTarget = (20 * scale).toInt().coerceAtLeast(10),
            listeningMinutesTarget = (10 * scale).toInt().coerceAtLeast(5),
            professionalMinutesTarget = (10 * scale).toInt().coerceAtLeast(5),
            isNewVocabGatedByBacklog = gated
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailyPlanBreakdown(60, 10, 10, 20, 10, 10, false)
    )

    private val _diagnostics = MutableStateFlow<DeviceDiagnosticsReport?>(null)
    val diagnostics: StateFlow<DeviceDiagnosticsReport?> = _diagnostics.asStateFlow()

    private val _featureMatrix = MutableStateFlow<List<FeatureMatrixItem>>(emptyList())
    val featureMatrix: StateFlow<List<FeatureMatrixItem>> = _featureMatrix.asStateFlow()

    private val _speechModels = MutableStateFlow<List<SpeechModelSpec>>(emptyList())
    val speechModels: StateFlow<List<SpeechModelSpec>> = _speechModels.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = DeviceDiagnosticsManager.isOnline(context)
        }

        override fun onLost(network: Network) {
            _isOnline.value = DeviceDiagnosticsManager.isOnline(context)
        }
    }

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            refreshDeviceDiagnostics()
        }
        try {
            val cm = context.getSystemService(ConnectivityManager::class.java)
            cm?.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        } catch (_: Exception) {
        }
    }

    fun refreshConnectivity() {
        _isOnline.value = DeviceDiagnosticsManager.isOnline(context)
    }

    fun refreshDeviceDiagnostics() {
        ttsManager.refreshVoiceCapabilities()
        speechManager.refreshDiagnostics()
        val report = DeviceDiagnosticsManager.collectDiagnostics(
            context = context,
            ttsStatus = ttsManager.status.value,
            speechDiagnostics = speechManager.diagnostics.value
        )
        _diagnostics.value = report
        _featureMatrix.value = DeviceDiagnosticsManager.buildFeatureStatusMatrix(report)
        _speechModels.value = speechManager.getAvailableModelSpecs()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun updateCefrFilter(cefr: String) {
        _selectedCefr.value = cefr
    }

    private val aiCoachService = com.example.ai.AiSpeakingCoachService()

    fun speakEnglish(text: String, accentOverride: String? = null, speed: Float = 1.0f) {
        val currentProfile = profile.value
        val accent = accentOverride ?: currentProfile.accentPreference
        val ok = ttsManager.speak(
            text = text,
            accentPreference = accent,
            speechRate = speed,
            isOnline = _isOnline.value,
            voiceGender = currentProfile.voiceGender
        )
        if (!ok) {
            _statusBanner.value = ttsManager.status.value.errorMessage
                ?: "Offline TTS voice unavailable on this device."
        }
    }

    fun setVoiceGender(gender: String) {
        val normalized = if (gender == "MALE") "MALE" else "FEMALE"
        viewModelScope.launch {
            repository.updateProfile { it.copy(voiceGender = normalized) }
        }
    }

    suspend fun generateFlashcardSentence(
        word: VocabWordEntity,
        variantIndex: Int = 0
    ): VocabExample {
        return aiCoachService.generateFlashcardSentence(
            word = word,
            isOnline = _isOnline.value,
            variantIndex = variantIndex
        )
    }

    fun setAccentPreference(accent: String) {
        viewModelScope.launch {
            repository.updateProfile { it.copy(accentPreference = accent) }
        }
    }

    fun setUiLanguage(lang: String) {
        viewModelScope.launch {
            repository.updateProfile { it.copy(uiLanguage = lang) }
        }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch {
            repository.updateProfile { it.copy(darkModePreference = mode) }
        }
    }

    fun setFontScale(scale: Float) {
        val clamped = scale.coerceIn(0.80f, 1.40f)
        viewModelScope.launch {
            repository.updateProfile { it.copy(fontScale = clamped) }
        }
    }

    fun setDailyTargetMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.updateProfile { it.copy(dailyMinutesTarget = minutes) }
        }
    }

    fun submitVocabReview(
        word: VocabWordEntity,
        rating: FsrsScheduler.Rating,
        mode: String,
        responseTimeMs: Long,
        userSentence: String = ""
    ) {
        viewModelScope.launch {
            repository.recordVocabReview(word, rating, mode, responseTimeMs, userSentence)
        }
    }

    fun updateSelfRating(word: VocabWordEntity, rating: String) {
        viewModelScope.launch {
            repository.updateWordSelfRating(word, rating)
        }
    }

    fun submitAssessment(
        vocabScore: Int,
        grammarScore: Int,
        listeningScore: Int,
        speakingScore: Int,
        pronunciationScore: Int
    ) {
        viewModelScope.launch {
            repository.saveAssessmentResult(
                vocabScore, grammarScore, listeningScore, speakingScore, pronunciationScore
            )
            _statusBanner.value = "Assessment profile saved locally."
        }
    }

    fun markLessonComplete(lesson: LessonModuleEntity) {
        viewModelScope.launch {
            repository.markLessonCompleted(lesson)
        }
    }

    fun saveShadowingRecording(title: String, filePath: String, durationMs: Long, transcript: String = "") {
        viewModelScope.launch {
            repository.saveRecording(
                SpeakingRecordingEntity(
                    title = title,
                    contextType = "SHADOWING",
                    filePath = filePath,
                    durationMs = durationMs,
                    transcript = transcript,
                    transcriptionProvider = if (transcript.isNotBlank()) "MANUAL_OR_ASR" else "NONE",
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
        }
    }

    fun installOrDownloadPackage(pkg: ContentPackageEntity) {
        packageManager.startOrResumePackageInstall(pkg)
    }

    fun pauseDownload(itemId: String) {
        packageManager.pauseDownload(itemId)
    }

    fun deletePackage(pkg: ContentPackageEntity) {
        viewModelScope.launch {
            packageManager.deletePackage(pkg)
        }
    }

    fun downloadVoskModel(spec: SpeechModelSpec) {
        if (!_isOnline.value) {
            _statusBanner.value = "Internet required to download ${spec.name} (~40 MB)."
            return
        }
        packageManager.downloadVoskModel(spec)
        _statusBanner.value = "WorkManager download queued for ${spec.name}."
    }

    fun removeVoskModel(modelId: String) {
        speechManager.removeModel(modelId)
        refreshDeviceDiagnostics()
    }

    fun exportBackupToUri(uri: Uri, includeRecordings: Boolean) {
        viewModelScope.launch {
            val res = backupRepository.exportToUri(uri, includeRecordings)
            _statusBanner.value = res.fold(
                onSuccess = { bytes ->
                    val kb = (bytes / 1024).coerceAtLeast(1)
                    if (profile.value.uiLanguage == "FA") {
                        "پشتیبان کامل برنامه با موفقیت روی گوشی ذخیره شد ($kb کیلوبایت)."
                    } else {
                        "Full app backup saved successfully ($kb KB)."
                    }
                },
                onFailure = { err -> "Export failed: ${err.localizedMessage}" }
            )
        }
    }

    fun prepareBackupForShare(
        includeRecordings: Boolean,
        onReady: (Uri) -> Unit
    ) {
        viewModelScope.launch {
            val res = backupRepository.createShareableBackupFile(includeRecordings)
            res.fold(
                onSuccess = { (_, uri) -> onReady(uri) },
                onFailure = { err ->
                    _statusBanner.value = "Share preparation failed: ${err.localizedMessage}"
                }
            )
        }
    }

    fun importBackupFromUri(uri: Uri) {
        viewModelScope.launch {
            val res = backupRepository.importFromUri(uri)
            _statusBanner.value = res.fold(
                onSuccess = { s ->
                    if (profile.value.uiLanguage == "FA") {
                        "بازیابی کامل انجام شد: ${s.vocabCount} واژه، ${s.studyDaysCount} روز مطالعه، ${s.sessionsCount} مکالمه، ${s.lessonsCount} درس."
                    } else {
                        "Restored full backup: ${s.vocabCount} words, ${s.studyDaysCount} study days, ${s.sessionsCount} chats, ${s.lessonsCount} lessons."
                    }
                },
                onFailure = { err -> "Restore failed: ${err.localizedMessage}" }
            )
        }
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    override fun onCleared() {
        super.onCleared()
        try {
            val cm = context.getSystemService(ConnectivityManager::class.java)
            cm?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
        }
        ttsManager.shutdown()
        audioRecorder.release()
        speechManager.stopListening()
    }
}
