package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AiSpeakingCoachService
import com.example.data.local.EnglishProDatabase
import com.example.data.local.StarterLessonsAndScenariosSeed
import com.example.data.local.StarterVocabularySeed
import com.example.data.repository.EnglishProRepository
import com.example.domain.backup.BackupRepository
import com.example.domain.srs.FsrsScheduler
import com.example.ui.localization.AppStrings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EnglishProCoreTest {

    private lateinit var context: Context
    private lateinit var db: EnglishProDatabase
    private lateinit var repo: EnglishProRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, EnglishProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = EnglishProRepository(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `seed database populates profile, vocabulary, 20 roleplays, and 10 packages`() = runTest {
        repo.ensureSeeded()

        val profile = db.userProfileDao().getProfileOnce()
        assertNotNull(profile)
        assertEquals(60, profile?.dailyMinutesTarget)
        assertEquals("FA", profile?.uiLanguage)

        val vocab = db.vocabDao().getAllVocabOnce()
        assertTrue("Starter vocab should have >= 50 entries", vocab.size >= 50)

        val scenarios = repo.scenarios.first()
        assertEquals(20, scenarios.size)

        val packages = repo.packages.first()
        assertEquals(10, packages.size)
    }

    @Test
    fun `assessment saves CEFR profile and learning band note`() = runTest {
        repo.ensureSeeded()
        val record = repo.saveAssessmentResult(
            vocabScore = 78,
            grammarScore = 70,
            listeningScore = 74,
            speakingScore = 62,
            pronunciationScore = 64
        )
        assertTrue(record.estimatedLearningNote.contains("Unofficial practice estimate"))
        val updated = db.userProfileDao().getProfileOnce()!!
        assertTrue(updated.initialAssessmentCompleted)
        assertEquals("B2+", updated.overallCefr)
    }

    @Test
    fun `FSRS scheduler updates stability, due date, passive-to-active status, and backlog gate`() = runTest {
        repo.ensureSeeded()
        val words = db.vocabDao().getAllVocabOnce().take(20)

        // Learn 20 vocabulary items with Good rating in Production mode
        words.forEach { w ->
            repo.recordVocabReview(
                word = w,
                rating = FsrsScheduler.Rating.GOOD,
                mode = "PRODUCTION",
                responseTimeMs = 3000L,
                userSentence = "We verified the ${w.word} on site today."
            )
        }

        val logs = db.fsrsDao().getAllReviewLogsOnce()
        assertEquals(20, logs.size)

        val firstCard = db.fsrsDao().getCard(words.first().id)!!
        assertTrue(firstCard.scheduledDays >= 1)
        assertTrue(firstCard.dueAtEpochMs > System.currentTimeMillis())

        assertTrue(FsrsScheduler.shouldGateNewVocabulary(dueCount = 26, threshold = 25))
        assertFalse(FsrsScheduler.shouldGateNewVocabulary(dueCount = 10, threshold = 25))
    }

    @Test
    fun `offline vocabulary search finds English and Persian entries`() = runTest {
        repo.ensureSeeded()
        val enMatches = db.vocabDao().searchVocab("clarifier", "ALL", "ALL").first()
        assertTrue(enMatches.any { it.word == "clarifier" })

        val faMatches = db.vocabDao().searchVocab("گودبرداری", "ALL", "ALL").first()
        assertTrue(faMatches.any { it.word == "excavation" })
    }

    @Test
    fun `offline coach fallback handles construction roleplay without internet`() = runTest {
        val scenarios = StarterLessonsAndScenariosSeed.getRolePlayScenarios()
        val rp = scenarios.first()
        val service = AiSpeakingCoachService()
        val reply = service.generateOfflineFallbackReply(
            userMessage = "We checked the equipments and formwork before pouring.",
            turnIndex = 0,
            scenario = rp,
            targetWords = listOf("push back", "curing")
        )
        assertFalse(reply.isFromOnlineAi)
        assertEquals("INCORRECT", reply.correction.category)
        assertTrue(reply.englishReply.isNotBlank())
    }

    @Test
    fun `JSON backup export and restore preserves vocabulary progress and assessments`() = runTest {
        repo.ensureSeeded()
        repo.saveAssessmentResult(80, 75, 72, 68, 70)
        val word = db.vocabDao().getAllVocabOnce().first()
        repo.recordVocabReview(word, FsrsScheduler.Rating.EASY, "PRODUCTION", 2500L, "Sample test sentence.")

        val backupRepo = BackupRepository(context, db)
        val jsonBackup = backupRepo.createBackupJson(includeRecordingsMetadata = false)

        // Restore into a brand-new clean database
        val restoredDb = Room.inMemoryDatabaseBuilder(context, EnglishProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val restoredBackupRepo = BackupRepository(context, restoredDb)
        val result = restoredBackupRepo.restoreFromJson(jsonBackup)
        assertTrue(result.isSuccess)

        val restoredProfile = restoredDb.userProfileDao().getProfileOnce()!!
        assertTrue(restoredProfile.initialAssessmentCompleted)
        assertEquals(1, restoredDb.analyticsDao().getAllAssessmentsOnce().size)
        assertEquals(1, restoredDb.fsrsDao().getAllReviewLogsOnce().size)
        restoredDb.close()
    }

    @Test
    fun `bilingual strings provide both Persian RTL and English LTR modes`() {
        assertTrue(AppStrings.forLang("FA").isPersian)
        assertFalse(AppStrings.forLang("EN").isPersian)
        assertTrue(StarterVocabularySeed.getExpansionWordsForPackage("pkg_wastewater", 0L).isNotEmpty())
    }

    @Test
    fun `user profile persists font scale and voice gender changes cleanly`() = runTest {
        repo.ensureSeeded()
        val initial = db.userProfileDao().getProfileOnce()!!
        assertEquals(1.0f, initial.fontScale, 0.01f)
        assertEquals("FEMALE", initial.voiceGender)

        db.userProfileDao().upsertProfile(initial.copy(fontScale = 1.25f, voiceGender = "MALE"))
        val updated = db.userProfileDao().getProfileOnce()!!
        assertEquals(1.25f, updated.fontScale, 0.01f)
        assertEquals("MALE", updated.voiceGender)
    }

    @Test
    fun `voice gender selector classifies male and female TTS voice names accurately`() {
        assertEquals("FEMALE", com.example.speech.VoiceGenderSelector.classifyVoiceName("en-us-x-sfg-local"))
        assertEquals("MALE", com.example.speech.VoiceGenderSelector.classifyVoiceName("en-us-x-iol-local"))
        assertEquals("FEMALE", com.example.speech.VoiceGenderSelector.classifyVoiceName("en-gb-female-voice"))
        assertEquals("MALE", com.example.speech.VoiceGenderSelector.classifyVoiceName("en-gb-male-voice"))
    }

    @Test
    fun `flashcard offline sentence generator produces bilingual sentences using target word`() = runTest {
        repo.ensureSeeded()
        val word = db.vocabDao().getAllVocabOnce().first()
        val service = AiSpeakingCoachService()
        val generated = service.buildOfflineFlashcardSentence(word, variantIndex = 0)
        assertTrue(generated.english.isNotBlank())
        assertTrue(generated.persian.isNotBlank())
    }

    @Test
    fun `full app backup preserves appearance settings, 5-day study logs, lessons, packages, scenarios, and recordings`() = runTest {
        repo.ensureSeeded()
        repo.updateProfile {
            it.copy(
                darkModePreference = "DARK",
                fontScale = 1.25f,
                voiceGender = "MALE",
                uiLanguage = "EN",
                dailyMinutesTarget = 90
            )
        }
        // Simulate 5 days of active learning history
        val fiveDays = listOf(
            com.example.data.local.StudyLogEntity("2026-11-14", vocabMinutes = 15, srsMinutes = 10, speakingMinutes = 20, newWordsLearned = 8),
            com.example.data.local.StudyLogEntity("2026-11-15", vocabMinutes = 12, srsMinutes = 12, speakingMinutes = 25, reviewsCompleted = 14),
            com.example.data.local.StudyLogEntity("2026-11-16", vocabMinutes = 10, listeningMinutes = 15, proMinutes = 10, speakingTurns = 12),
            com.example.data.local.StudyLogEntity("2026-11-17", vocabMinutes = 15, srsMinutes = 15, speakingMinutes = 20, newWordsLearned = 6),
            com.example.data.local.StudyLogEntity("2026-11-18", vocabMinutes = 20, srsMinutes = 10, speakingMinutes = 30, reviewsCompleted = 18)
        )
        db.analyticsDao().upsertStudyLogs(fiveDays)

        val firstLesson = db.lessonAndRolePlayDao().getAllLessonsOnce().first()
        repo.markLessonCompleted(firstLesson)

        val firstScenario = db.lessonAndRolePlayDao().getAllScenariosOnce().first()
        db.lessonAndRolePlayDao().updateScenario(firstScenario.copy(completedCount = 2, bestScore = 88))

        db.recordingDao().insertRecording(
            com.example.data.local.SpeakingRecordingEntity(
                title = "Site Check Recording",
                contextType = "COACH",
                filePath = "/tmp/nonexistent_sample.m4a",
                durationMs = 4500L,
                transcript = "The curing compound was applied evenly.",
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val backupRepo = BackupRepository(context, db)
        val fullJson = backupRepo.createBackupJson(includeRecordingsMetadata = true)

        val restoredDb = Room.inMemoryDatabaseBuilder(context, EnglishProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val restoreRes = BackupRepository(context, restoredDb).restoreFromJson(fullJson)
        assertTrue(restoreRes.isSuccess)
        val summary = restoreRes.getOrThrow()

        val restoredProfile = restoredDb.userProfileDao().getProfileOnce()!!
        assertEquals("DARK", restoredProfile.darkModePreference)
        assertEquals(1.25f, restoredProfile.fontScale, 0.01f)
        assertEquals("MALE", restoredProfile.voiceGender)
        assertEquals("EN", restoredProfile.uiLanguage)
        assertEquals(90, restoredProfile.dailyMinutesTarget)

        val restoredLogs = restoredDb.analyticsDao().getAllStudyLogsOnce()
        assertTrue(restoredLogs.size >= 5)
        assertEquals(5, summary.studyDaysCount.coerceAtMost(5))
        assertTrue(restoredDb.lessonAndRolePlayDao().getAllLessonsOnce().any { it.isCompleted })
        assertEquals(88, restoredDb.lessonAndRolePlayDao().getScenario(firstScenario.id)?.bestScore)
        assertEquals(10, restoredDb.packageDao().getAllPackagesOnce().size)
        assertEquals(1, restoredDb.recordingDao().getAllRecordingsOnce().size)

        restoredDb.close()
    }
}
