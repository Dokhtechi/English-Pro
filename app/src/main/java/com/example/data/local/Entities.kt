package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val learnerName: String = "Senior Civil Engineer",
    val targetLevel: String = "C1",
    val overallCefr: String = "B2",
    val speakingCefr: String = "B1+",
    val listeningCefr: String = "B2",
    val vocabularyCefr: String = "B2",
    val grammarCefr: String = "B2",
    val pronunciationCefr: String = "B1+",
    val dailyMinutesTarget: Int = 60, // 30, 60, 90
    val accentPreference: String = "INTERNATIONAL", // US, UK, INTERNATIONAL
    val technicalDefaultVoice: String = "US",
    val uiLanguage: String = "FA", // FA (default RTL), EN (LTR)
    val darkModePreference: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val fontScale: Float = 1.0f, // 0.85f .. 1.35f
    val voiceGender: String = "FEMALE", // FEMALE, MALE
    val initialAssessmentCompleted: Boolean = false,
    val lastAssessmentAtEpochMs: Long = 0L,
    val streakDays: Int = 0,
    val lastStudyDateIso: String = "",
    val totalStudyMinutes: Int = 0,
    val overloadBacklogThreshold: Int = 25
)

@Serializable
@Entity(
    tableName = "vocab_words",
    indices = [
        Index(value = ["category"]),
        Index(value = ["cefrLevel"]),
        Index(value = ["packageId"]),
        Index(value = ["word"])
    ]
)
data class VocabWordEntity(
    @PrimaryKey val id: String,
    val word: String,
    val ipaUs: String,
    val ipaUk: String,
    val partOfSpeech: String,
    val cefrLevel: String,
    val frequencyRank: Int,
    val topic: String,
    val category: String, // GENERAL, CIVIL_SITE, CONCRETE_STRUCTURAL, GEOTECHNICAL, WATER_WASTEWATER, PROJECT_MANAGEMENT, FIDIC, SAFETY, MEETINGS
    val register: String, // TECHNICAL, FORMAL, NEUTRAL, INFORMAL
    val persianMeaning: String,
    val englishDefinition: String,
    val examplesJson: String, // List<VocabExample>
    val collocationsJson: String, // List<String>
    val synonymsJson: String, // List<String>
    val antonymsJson: String, // List<String>
    val wordFamilyJson: String, // List<String>
    val commonMistakes: String,
    val britishVsAmericanNote: String,
    val sourceAttribution: String,
    val licenseInfo: String,
    val packageId: String,
    val selfRating: String = "NEW", // NEW, DIFFICULT, PARTIAL, KNOWN, EASY
    val isPassiveOnly: Boolean = false,
    val isMasteredActive: Boolean = false,
    val learnedFromConversation: Boolean = false,
    val createdAtEpochMs: Long = 0L
)

@Serializable
data class VocabExample(
    val english: String,
    val persian: String
)

@Serializable
@Entity(
    tableName = "fsrs_cards",
    foreignKeys = [
        ForeignKey(
            entity = VocabWordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["dueAtEpochMs"]), Index(value = ["state"])]
)
data class FsrsCardEntity(
    @PrimaryKey val wordId: String,
    val stability: Double = 0.0,
    val difficulty: Double = 5.0,
    val elapsedDays: Int = 0,
    val scheduledDays: Int = 0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val state: Int = 0, // 0=New, 1=Learning, 2=Review, 3=Relearning
    val dueAtEpochMs: Long = 0L,
    val lastReviewAtEpochMs: Long = 0L,
    val avgResponseTimeMs: Long = 0L,
    val recognitionPassCount: Int = 0,
    val productionPassCount: Int = 0,
    val lastUserSentence: String = ""
)

@Serializable
@Entity(tableName = "review_logs", indices = [Index(value = ["wordId"]), Index(value = ["reviewedAtEpochMs"])])
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val wordId: String,
    val rating: Int, // 1=Again, 2=Hard, 3=Good, 4=Easy
    val mode: String, // RECOGNITION, PRODUCTION, SPEAKING
    val responseTimeMs: Long,
    val reviewedAtEpochMs: Long
)

@Serializable
@Entity(tableName = "conversation_sessions", indices = [Index(value = ["startedAtEpochMs"])])
data class ConversationSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mode: String, // FREE_COACH, ROLEPLAY, ASSESSMENT
    val correctionMode: String, // FLUENCY, ACCURACY
    val scenarioId: String? = null,
    val title: String,
    val isOnlineAi: Boolean,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long = 0L,
    val durationSeconds: Int = 0,
    val fluencyScore: Int = 0,
    val vocabRangeScore: Int = 0,
    val grammarScore: Int = 0,
    val complexityScore: Int = 0,
    val pronunciationScore: Int = 0,
    val spontaneousScore: Int = 0,
    val hesitationNotes: String = "",
    val targetWordsUsedJson: String = "[]",
    val feedbackSummary: String = "",
    val recommendationsJson: String = "[]"
)

@Serializable
@Entity(
    tableName = "conversation_turns",
    foreignKeys = [
        ForeignKey(
            entity = ConversationSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class ConversationTurnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val speaker: String, // USER, COACH
    val englishText: String,
    val persianTranslation: String = "",
    val correctionJson: String = "", // TurnCorrection JSON
    val audioFilePath: String? = null,
    val createdAtEpochMs: Long
)

@Serializable
data class TurnCorrection(
    val category: String = "NATURAL", // INCORRECT, UNNATURAL, NATURAL
    val learnerSaid: String = "",
    val correctedSentence: String = "",
    val naturalAlternative: String = "",
    val explanation: String = "",
    val persianNote: String = ""
)

@Serializable
@Entity(tableName = "speaking_recordings", indices = [Index(value = ["createdAtEpochMs"])])
data class SpeakingRecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val contextType: String, // COACH, SHADOWING, ASSESSMENT, VOCAB_SPEAKING
    val filePath: String,
    val durationMs: Long,
    val transcript: String = "",
    val transcriptionProvider: String = "NONE", // ANDROID_ON_DEVICE, MANUAL, VOSK_MODEL_READY
    val createdAtEpochMs: Long
)

@Serializable
@Entity(tableName = "content_packages")
data class ContentPackageEntity(
    @PrimaryKey val id: String,
    val title: String,
    val titleFa: String,
    val category: String,
    val description: String,
    val descriptionFa: String,
    val sizeBytes: Long,
    val version: String,
    val licenseName: String,
    val attribution: String,
    val downloadUrl: String,
    val status: String, // NOT_INSTALLED, DOWNLOADING, PAUSED, INSTALLED, FAILED
    val downloadedBytes: Long = 0L,
    val progressPercent: Int = 0,
    val installedItemCount: Int = 0,
    val localPath: String = "",
    val checksumSha256: String = "",
    val updatedAtEpochMs: Long = 0L
)

@Serializable
@Entity(tableName = "lesson_modules", indices = [Index(value = ["category"]), Index(value = ["packageId"])])
data class LessonModuleEntity(
    @PrimaryKey val id: String,
    val packageId: String,
    val category: String, // D1_SITE, D2_CONCRETE, D3_GEOTECH, D4_WASTEWATER, D5_PM_FIDIC, D6_SAFETY, D7_MEETINGS, LISTENING_GENERAL, LISTENING_WORKPLACE
    val title: String,
    val titleFa: String,
    val accent: String, // US, UK, INT
    val difficultyCefr: String,
    val durationMinutes: Int,
    val vocabularyIdsJson: String,
    val naturalSentencesJson: String, // List<VocabExample>
    val dialogueTurnsJson: String, // List<LessonDialogueTurn>
    val comprehensionQuestionsJson: String, // List<ComprehensionQuestion>
    val difficultExpressionsJson: String, // List<ExpressionNote>
    val practicalSpeakingTask: String,
    val reviewQuestionsJson: String, // List<String>
    val britishVsAmericanNotes: String,
    val isCompleted: Boolean = false,
    val completedAtEpochMs: Long = 0L
)

@Serializable
data class LessonDialogueTurn(
    val speaker: String,
    val accent: String = "US",
    val text: String,
    val persianHint: String = ""
)

@Serializable
data class ComprehensionQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Serializable
data class ExpressionNote(
    val expression: String,
    val meaningEn: String,
    val meaningFa: String,
    val registerNote: String
)

@Serializable
@Entity(tableName = "role_play_scenarios", indices = [Index(value = ["category"])])
data class RolePlayScenarioEntity(
    @PrimaryKey val id: String,
    val number: Int,
    val title: String,
    val titleFa: String,
    val category: String,
    val aiRole: String,
    val difficultyCefr: String,
    val objective: String,
    val background: String,
    val complicationPrompt: String,
    val usefulExpressionsJson: String, // List<ExpressionNote>
    val targetVocabJson: String, // List<String>
    val offlineBranchingJson: String, // List<OfflineBranchNode>
    val completedCount: Int = 0,
    val bestScore: Int = 0
)

@Serializable
data class OfflineBranchNode(
    val stepIndex: Int,
    val aiPrompt: String,
    val suggestedResponses: List<String>,
    val expectedKeywords: List<String>,
    val coachHintFa: String
)

@Serializable
@Entity(tableName = "assessment_records", indices = [Index(value = ["takenAtEpochMs"])])
data class AssessmentRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val takenAtEpochMs: Long,
    val overallCefr: String,
    val estimatedLearningNote: String,
    val vocabScore: Int,
    val grammarScore: Int,
    val listeningScore: Int,
    val speakingScore: Int,
    val pronunciationScore: Int,
    val weakAreasJson: String,
    val recommendedPlanJson: String
)

@Serializable
@Entity(tableName = "study_logs")
data class StudyLogEntity(
    @PrimaryKey val dateIso: String, // YYYY-MM-DD
    val vocabMinutes: Int = 0,
    val srsMinutes: Int = 0,
    val speakingMinutes: Int = 0,
    val listeningMinutes: Int = 0,
    val proMinutes: Int = 0,
    val newWordsLearned: Int = 0,
    val reviewsCompleted: Int = 0,
    val speakingTurns: Int = 0
) {
    val totalMinutes: Int
        get() = vocabMinutes + srsMinutes + speakingMinutes + listeningMinutes + proMinutes
}

@Serializable
@Entity(tableName = "grammar_error_patterns")
data class GrammarErrorPatternEntity(
    @PrimaryKey val patternKey: String,
    val patternLabel: String,
    val count: Int,
    val lastExample: String,
    val updatedAtEpochMs: Long
)
