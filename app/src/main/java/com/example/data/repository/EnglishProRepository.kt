package com.example.data.repository

import com.example.data.local.*
import com.example.domain.srs.FsrsScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EnglishProRepository(private val db: EnglishProDatabase) {
    private val json = Json { ignoreUnknownKeys = true }

    val profile: Flow<UserProfileEntity> = db.userProfileDao().observeProfile().map { it ?: UserProfileEntity() }
    val allVocab: Flow<List<VocabWordEntity>> = db.vocabDao().observeAllVocab()
    val vocabCount: Flow<Int> = db.vocabDao().observeVocabCount()
    val activeMasteredCount: Flow<Int> = db.vocabDao().observeActiveMasteredCount()
    val passiveOnlyCount: Flow<Int> = db.vocabDao().observePassiveOnlyCount()
    val civilMasteredCount: Flow<Int> = db.vocabDao().observeCivilMasteredCount()

    val allFsrsCards: Flow<List<FsrsCardEntity>> = db.fsrsDao().observeAllCards()
    val recentReviewLogs: Flow<List<ReviewLogEntity>> = db.fsrsDao().observeRecentLogs()

    val conversationSessions: Flow<List<ConversationSessionEntity>> = db.conversationDao().observeSessions()
    val recordings: Flow<List<SpeakingRecordingEntity>> = db.recordingDao().observeRecordings()
    val packages: Flow<List<ContentPackageEntity>> = db.packageDao().observePackages()
    val lessons: Flow<List<LessonModuleEntity>> = db.lessonAndRolePlayDao().observeLessons()
    val scenarios: Flow<List<RolePlayScenarioEntity>> = db.lessonAndRolePlayDao().observeScenarios()
    val assessments: Flow<List<AssessmentRecordEntity>> = db.analyticsDao().observeAssessments()
    val studyLogs: Flow<List<StudyLogEntity>> = db.analyticsDao().observeRecentStudyLogs()
    val errorPatterns: Flow<List<GrammarErrorPatternEntity>> = db.analyticsDao().observeErrorPatterns()

    fun observeDueCards(nowMs: Long = System.currentTimeMillis()): Flow<List<FsrsCardEntity>> =
        db.fsrsDao().observeDueCards(nowMs)

    fun observeDueCount(nowMs: Long = System.currentTimeMillis()): Flow<Int> =
        db.fsrsDao().observeDueCount(nowMs)

    fun searchVocab(query: String, category: String, cefr: String): Flow<List<VocabWordEntity>> =
        db.vocabDao().searchVocab(query, category, cefr)

    fun observeTurns(sessionId: Long): Flow<List<ConversationTurnEntity>> =
        db.conversationDao().observeTurnsForSession(sessionId)

    suspend fun ensureSeeded() {
        EnglishProDatabase.seedIfEmpty(db)
    }

    suspend fun updateProfile(transform: (UserProfileEntity) -> UserProfileEntity) {
        val current = db.userProfileDao().getProfileOnce() ?: UserProfileEntity()
        db.userProfileDao().upsertProfile(transform(current))
    }

    suspend fun recordVocabReview(
        word: VocabWordEntity,
        rating: FsrsScheduler.Rating,
        mode: String,
        responseTimeMs: Long,
        userSentence: String = ""
    ) {
        val now = System.currentTimeMillis()
        val existingCard = db.fsrsDao().getCard(word.id) ?: FsrsCardEntity(wordId = word.id)
        val (newCard, updatedWord) = FsrsScheduler.scheduleNext(
            card = existingCard,
            word = word,
            rating = rating,
            mode = mode,
            responseTimeMs = responseTimeMs,
            userSentence = userSentence,
            nowMs = now
        )
        val log = ReviewLogEntity(
            wordId = word.id,
            rating = rating.value,
            mode = mode,
            responseTimeMs = responseTimeMs,
            reviewedAtEpochMs = now
        )
        db.fsrsDao().recordReview(newCard, log, updatedWord, db.vocabDao())
        addStudyProgress(
            srsMin = if (existingCard.state > 0) 1 else 0,
            vocabMin = if (existingCard.state == 0) 1 else 0,
            newWords = if (existingCard.state == 0) 1 else 0,
            reviews = if (existingCard.state > 0) 1 else 0
        )
    }

    suspend fun updateWordSelfRating(word: VocabWordEntity, selfRating: String) {
        db.vocabDao().updateWord(word.copy(selfRating = selfRating))
    }

    suspend fun startConversationSession(
        mode: String,
        correctionMode: String,
        scenarioId: String?,
        title: String,
        isOnlineAi: Boolean
    ): Long {
        return db.conversationDao().insertSession(
            ConversationSessionEntity(
                mode = mode,
                correctionMode = correctionMode,
                scenarioId = scenarioId,
                title = title,
                isOnlineAi = isOnlineAi,
                startedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun addConversationTurn(turn: ConversationTurnEntity): Long {
        val id = db.conversationDao().insertTurn(turn)
        if (turn.speaker == "USER") {
            addStudyProgress(speakingMin = 1, speakingTurns = 1)
        }
        return id
    }

    suspend fun completeConversationSession(
        session: ConversationSessionEntity,
        fluency: Int,
        vocabRange: Int,
        grammar: Int,
        complexity: Int,
        pronunciation: Int,
        spontaneous: Int,
        hesitationNotes: String,
        targetWordsUsed: List<String>,
        summary: String,
        recommendations: List<String>
    ) {
        val now = System.currentTimeMillis()
        val durationSec = ((now - session.startedAtEpochMs) / 1000L).toInt().coerceAtLeast(30)
        db.conversationDao().updateSession(
            session.copy(
                endedAtEpochMs = now,
                durationSeconds = durationSec,
                fluencyScore = fluency,
                vocabRangeScore = vocabRange,
                grammarScore = grammar,
                complexityScore = complexity,
                pronunciationScore = pronunciation,
                spontaneousScore = spontaneous,
                hesitationNotes = hesitationNotes,
                targetWordsUsedJson = json.encodeToString(targetWordsUsed),
                feedbackSummary = summary,
                recommendationsJson = json.encodeToString(recommendations)
            )
        )
        session.scenarioId?.let { scId ->
            db.lessonAndRolePlayDao().getScenario(scId)?.let { sc ->
                val avg = (fluency + vocabRange + grammar + spontaneous) / 4
                db.lessonAndRolePlayDao().updateScenario(
                    sc.copy(
                        completedCount = sc.completedCount + 1,
                        bestScore = maxOf(sc.bestScore, avg)
                    )
                )
            }
        }
        addStudyProgress(speakingMin = (durationSec / 60).coerceAtLeast(1))
    }

    suspend fun saveRecording(recording: SpeakingRecordingEntity): Long {
        return db.recordingDao().insertRecording(recording)
    }

    suspend fun updateRecordingTranscript(recording: SpeakingRecordingEntity, transcript: String, provider: String) {
        db.recordingDao().updateRecording(recording.copy(transcript = transcript, transcriptionProvider = provider))
    }

    suspend fun markLessonCompleted(lesson: LessonModuleEntity) {
        db.lessonAndRolePlayDao().updateLesson(
            lesson.copy(isCompleted = true, completedAtEpochMs = System.currentTimeMillis())
        )
        addStudyProgress(listeningMin = 5, proMin = 5)
    }

    suspend fun saveAssessmentResult(
        vocabScore: Int,
        grammarScore: Int,
        listeningScore: Int,
        speakingScore: Int,
        pronunciationScore: Int
    ): AssessmentRecordEntity {
        val overallAvg = (vocabScore + grammarScore + listeningScore + speakingScore + pronunciationScore) / 5
        val overallCefr = scoreToCefr(overallAvg)
        val estimatedBand = when {
            overallAvg >= 85 -> "Estimated Learning Band: ~7.0–7.5 (Unofficial practice estimate, not an official IELTS score)"
            overallAvg >= 72 -> "Estimated Learning Band: ~6.5 (Unofficial practice estimate, not an official IELTS score)"
            overallAvg >= 58 -> "Estimated Learning Band: ~6.0 (Unofficial practice estimate, not an official IELTS score)"
            else -> "Estimated Learning Band: ~5.5 (Unofficial practice estimate, not an official IELTS score)"
        }

        val scoresMap = listOf(
            "Vocabulary" to vocabScore,
            "Grammar" to grammarScore,
            "Listening" to listeningScore,
            "Speaking" to speakingScore,
            "Pronunciation" to pronunciationScore
        ).sortedBy { it.second }
        val weakAreas = scoresMap.take(3).map { "${it.first} (${it.second}%)" }
        val plan = listOf(
            "40% Speaking & Role-Plays (20 min/day)",
            "25% Active Vocabulary & FSRS (20 min/day)",
            "15% Listening & Shadowing (10 min/day)",
            "20% Civil Engineering & Grammar Precision (10 min/day)"
        )

        val now = System.currentTimeMillis()
        val record = AssessmentRecordEntity(
            takenAtEpochMs = now,
            overallCefr = overallCefr,
            estimatedLearningNote = estimatedBand,
            vocabScore = vocabScore,
            grammarScore = grammarScore,
            listeningScore = listeningScore,
            speakingScore = speakingScore,
            pronunciationScore = pronunciationScore,
            weakAreasJson = json.encodeToString(weakAreas),
            recommendedPlanJson = json.encodeToString(plan)
        )
        db.analyticsDao().insertAssessment(record)

        updateProfile { profile ->
            profile.copy(
                overallCefr = overallCefr,
                vocabularyCefr = scoreToCefr(vocabScore),
                grammarCefr = scoreToCefr(grammarScore),
                listeningCefr = scoreToCefr(listeningScore),
                speakingCefr = scoreToCefr(speakingScore),
                pronunciationCefr = scoreToCefr(pronunciationScore),
                initialAssessmentCompleted = true,
                lastAssessmentAtEpochMs = now
            )
        }
        return record
    }

    suspend fun incrementErrorPattern(key: String, label: String, example: String) {
        val existing = db.analyticsDao().getAllErrorPatternsOnce().firstOrNull { it.patternKey == key }
        db.analyticsDao().upsertErrorPattern(
            GrammarErrorPatternEntity(
                patternKey = key,
                patternLabel = label,
                count = (existing?.count ?: 0) + 1,
                lastExample = example,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun addStudyProgress(
        vocabMin: Int = 0,
        srsMin: Int = 0,
        speakingMin: Int = 0,
        listeningMin: Int = 0,
        proMin: Int = 0,
        newWords: Int = 0,
        reviews: Int = 0,
        speakingTurns: Int = 0
    ) {
        val today = todayIso()
        val current = db.analyticsDao().getStudyLog(today) ?: StudyLogEntity(dateIso = today)
        val updated = current.copy(
            vocabMinutes = current.vocabMinutes + vocabMin,
            srsMinutes = current.srsMinutes + srsMin,
            speakingMinutes = current.speakingMinutes + speakingMin,
            listeningMinutes = current.listeningMinutes + listeningMin,
            proMinutes = current.proMinutes + proMin,
            newWordsLearned = current.newWordsLearned + newWords,
            reviewsCompleted = current.reviewsCompleted + reviews,
            speakingTurns = current.speakingTurns + speakingTurns
        )
        db.analyticsDao().upsertStudyLog(updated)

        val profile = db.userProfileDao().getProfileOnce() ?: UserProfileEntity()
        val totalAdded = vocabMin + srsMin + speakingMin + listeningMin + proMin
        val newStreak = when {
            profile.lastStudyDateIso == today -> maxOf(1, profile.streakDays)
            else -> profile.streakDays + 1
        }
        db.userProfileDao().upsertProfile(
            profile.copy(
                streakDays = newStreak,
                lastStudyDateIso = today,
                totalStudyMinutes = profile.totalStudyMinutes + totalAdded
            )
        )
    }

    fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun scoreToCefr(score: Int): String = when {
        score >= 88 -> "C1+"
        score >= 78 -> "C1"
        score >= 66 -> "B2+"
        score >= 54 -> "B2"
        score >= 40 -> "B1+"
        else -> "B1"
    }
}
