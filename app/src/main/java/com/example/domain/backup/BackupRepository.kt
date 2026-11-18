package com.example.domain.backup

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class AppAppearanceBackup(
    val uiLanguage: String = "FA",
    val darkModePreference: String = "SYSTEM",
    val fontScale: Float = 1.0f,
    val voiceGender: String = "FEMALE",
    val accentPreference: String = "INTERNATIONAL",
    val technicalDefaultVoice: String = "US",
    val dailyMinutesTarget: Int = 60,
    val overloadBacklogThreshold: Int = 25
)

@Serializable
data class EmbeddedAudioFile(
    val originalPath: String,
    val fileName: String,
    val base64Content: String
)

@Serializable
data class EnglishProBackupPayload(
    val schemaVersion: Int = 2,
    val appName: String = "English Pro",
    val appVersion: String = "V.1.0",
    val exportedAtEpochMs: Long,
    val appearanceAndSettings: AppAppearanceBackup? = null,
    val profile: UserProfileEntity?,
    val vocabWords: List<VocabWordEntity> = emptyList(),
    val fsrsCards: List<FsrsCardEntity> = emptyList(),
    val reviewLogs: List<ReviewLogEntity> = emptyList(),
    val sessions: List<ConversationSessionEntity> = emptyList(),
    val turns: List<ConversationTurnEntity> = emptyList(),
    val assessments: List<AssessmentRecordEntity> = emptyList(),
    val studyLogs: List<StudyLogEntity> = emptyList(),
    val errorPatterns: List<GrammarErrorPatternEntity> = emptyList(),
    val contentPackages: List<ContentPackageEntity> = emptyList(),
    val lessons: List<LessonModuleEntity> = emptyList(),
    val scenarios: List<RolePlayScenarioEntity> = emptyList(),
    val recordingMetadata: List<SpeakingRecordingEntity> = emptyList(),
    val embeddedAudioFiles: List<EmbeddedAudioFile> = emptyList()
)

data class RestoreSummary(
    val vocabCount: Int,
    val studyDaysCount: Int,
    val sessionsCount: Int,
    val lessonsCount: Int,
    val assessmentsCount: Int,
    val recordingsCount: Int
)

class BackupRepository(
    private val context: Context,
    private val db: EnglishProDatabase
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun createBackupJson(includeRecordingsMetadata: Boolean): String = withContext(Dispatchers.IO) {
        val currentProfile = db.userProfileDao().getProfileOnce()
        val appearance = currentProfile?.let {
            AppAppearanceBackup(
                uiLanguage = it.uiLanguage,
                darkModePreference = it.darkModePreference,
                fontScale = it.fontScale,
                voiceGender = it.voiceGender,
                accentPreference = it.accentPreference,
                technicalDefaultVoice = it.technicalDefaultVoice,
                dailyMinutesTarget = it.dailyMinutesTarget,
                overloadBacklogThreshold = it.overloadBacklogThreshold
            )
        }

        val recordings = if (includeRecordingsMetadata) {
            db.recordingDao().getAllRecordingsOnce()
        } else emptyList()

        val embeddedAudio = mutableListOf<EmbeddedAudioFile>()
        if (includeRecordingsMetadata) {
            var totalEmbeddedBytes = 0L
            val maxSingleFileBytes = 1_500_000L
            val maxTotalBytes = 8_000_000L
            recordings.forEach { rec ->
                val f = File(rec.filePath)
                if (f.exists() && f.isFile && f.length() in 1..maxSingleFileBytes &&
                    totalEmbeddedBytes + f.length() <= maxTotalBytes
                ) {
                    runCatching {
                        val bytes = f.readBytes()
                        totalEmbeddedBytes += bytes.size
                        embeddedAudio += EmbeddedAudioFile(
                            originalPath = rec.filePath,
                            fileName = f.name,
                            base64Content = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        )
                    }
                }
            }
        }

        val payload = EnglishProBackupPayload(
            schemaVersion = 2,
            appName = "English Pro",
            appVersion = "V.1.0",
            exportedAtEpochMs = System.currentTimeMillis(),
            appearanceAndSettings = appearance,
            profile = currentProfile,
            vocabWords = db.vocabDao().getAllVocabOnce(),
            fsrsCards = db.fsrsDao().getAllCardsOnce(),
            reviewLogs = db.fsrsDao().getAllReviewLogsOnce(),
            sessions = db.conversationDao().getAllSessionsOnce(),
            turns = db.conversationDao().getAllTurnsOnce(),
            assessments = db.analyticsDao().getAllAssessmentsOnce(),
            studyLogs = db.analyticsDao().getAllStudyLogsOnce(),
            errorPatterns = db.analyticsDao().getAllErrorPatternsOnce(),
            contentPackages = db.packageDao().getAllPackagesOnce(),
            lessons = db.lessonAndRolePlayDao().getAllLessonsOnce(),
            scenarios = db.lessonAndRolePlayDao().getAllScenariosOnce(),
            recordingMetadata = recordings,
            embeddedAudioFiles = embeddedAudio
        )
        json.encodeToString(payload)
    }

    suspend fun exportToUri(uri: Uri, includeRecordingsMetadata: Boolean): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val content = createBackupJson(includeRecordingsMetadata)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
            } ?: error("Cannot open output stream")
            content.toByteArray(Charsets.UTF_8).size
        }
    }

    suspend fun createShareableBackupFile(includeRecordingsMetadata: Boolean): Result<Pair<File, Uri>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val content = createBackupJson(includeRecordingsMetadata)
                val dir = File(context.filesDir, "backups").apply { mkdirs() }
                val dateStamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date())
                val file = File(dir, "english_pro_full_backup_$dateStamp.json")
                file.writeText(content, Charsets.UTF_8)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                file to uri
            }
        }

    suspend fun restoreFromJson(jsonString: String): Result<RestoreSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = json.decodeFromString<EnglishProBackupPayload>(jsonString)

            // Reconstruct embedded audio recordings locally if present
            val restoredPathMap = mutableMapOf<String, String>()
            if (payload.embeddedAudioFiles.isNotEmpty()) {
                val recDir = File(context.filesDir, "recordings").apply { mkdirs() }
                payload.embeddedAudioFiles.forEach { audio ->
                    runCatching {
                        val safeName = File(audio.fileName).name
                        val dest = File(recDir, safeName)
                        val bytes = Base64.decode(audio.base64Content, Base64.DEFAULT)
                        dest.writeBytes(bytes)
                        restoredPathMap[audio.originalPath] = dest.absolutePath
                    }
                }
            }

            val restoredRecordings = payload.recordingMetadata.map { rec ->
                val mappedPath = restoredPathMap[rec.filePath]
                if (mappedPath != null) rec.copy(filePath = mappedPath) else rec
            }
            val restoredTurns = payload.turns.map { turn ->
                val mappedPath = turn.audioFilePath?.let { restoredPathMap[it] }
                if (mappedPath != null) turn.copy(audioFilePath = mappedPath) else turn
            }

            val mergedProfile = payload.profile?.let { p ->
                val app = payload.appearanceAndSettings
                if (app != null) {
                    p.copy(
                        uiLanguage = app.uiLanguage,
                        darkModePreference = app.darkModePreference,
                        fontScale = app.fontScale,
                        voiceGender = app.voiceGender,
                        accentPreference = app.accentPreference,
                        technicalDefaultVoice = app.technicalDefaultVoice,
                        dailyMinutesTarget = app.dailyMinutesTarget,
                        overloadBacklogThreshold = app.overloadBacklogThreshold
                    )
                } else p
            }

            db.withTransaction {
                mergedProfile?.let { db.userProfileDao().upsertProfile(it) }
                if (payload.contentPackages.isNotEmpty()) {
                    db.packageDao().upsertPackages(payload.contentPackages)
                }
                if (payload.vocabWords.isNotEmpty()) {
                    db.vocabDao().insertAll(payload.vocabWords)
                }
                if (payload.fsrsCards.isNotEmpty()) {
                    db.fsrsDao().upsertCards(payload.fsrsCards)
                }
                if (payload.reviewLogs.isNotEmpty()) {
                    db.fsrsDao().insertReviewLogs(payload.reviewLogs)
                }
                if (payload.lessons.isNotEmpty()) {
                    db.lessonAndRolePlayDao().upsertLessons(payload.lessons)
                }
                if (payload.scenarios.isNotEmpty()) {
                    db.lessonAndRolePlayDao().upsertScenarios(payload.scenarios)
                }
                if (payload.sessions.isNotEmpty()) {
                    db.conversationDao().insertSessions(payload.sessions)
                }
                if (restoredTurns.isNotEmpty()) {
                    db.conversationDao().insertTurns(restoredTurns)
                }
                if (payload.assessments.isNotEmpty()) {
                    db.analyticsDao().insertAssessments(payload.assessments)
                }
                if (payload.studyLogs.isNotEmpty()) {
                    db.analyticsDao().upsertStudyLogs(payload.studyLogs)
                }
                if (payload.errorPatterns.isNotEmpty()) {
                    db.analyticsDao().upsertErrorPatterns(payload.errorPatterns)
                }
                if (restoredRecordings.isNotEmpty()) {
                    db.recordingDao().insertRecordings(restoredRecordings)
                }
            }

            RestoreSummary(
                vocabCount = payload.vocabWords.size,
                studyDaysCount = payload.studyLogs.size,
                sessionsCount = payload.sessions.size,
                lessonsCount = payload.lessons.size,
                assessmentsCount = payload.assessments.size,
                recordingsCount = restoredRecordings.size
            )
        }
    }

    suspend fun importFromUri(uri: Uri): Result<RestoreSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            } ?: error("Cannot open backup file")
            restoreFromJson(jsonString).getOrThrow()
        }
    }

    fun saveInternalBackupSnapshot(jsonString: String): File {
        val dir = File(context.filesDir, "backups").apply { mkdirs() }
        val file = File(dir, "english_pro_snapshot.json")
        file.writeText(jsonString, Charsets.UTF_8)
        return file
    }
}
