package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        VocabWordEntity::class,
        FsrsCardEntity::class,
        ReviewLogEntity::class,
        ConversationSessionEntity::class,
        ConversationTurnEntity::class,
        SpeakingRecordingEntity::class,
        ContentPackageEntity::class,
        LessonModuleEntity::class,
        RolePlayScenarioEntity::class,
        AssessmentRecordEntity::class,
        StudyLogEntity::class,
        GrammarErrorPatternEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class EnglishProDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun vocabDao(): VocabDao
    abstract fun fsrsDao(): FsrsDao
    abstract fun conversationDao(): ConversationDao
    abstract fun recordingDao(): RecordingDao
    abstract fun packageDao(): PackageDao
    abstract fun lessonAndRolePlayDao(): LessonAndRolePlayDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: EnglishProDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN fontScale REAL NOT NULL DEFAULT 1.0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN voiceGender TEXT NOT NULL DEFAULT 'FEMALE'")
            }
        }

        fun getInstance(context: Context): EnglishProDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): EnglishProDatabase {
            return Room.databaseBuilder(
                context,
                EnglishProDatabase::class.java,
                "english_pro.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedIfEmpty(getInstance(context))
                        }
                    }
                })
                .build()
        }

        suspend fun seedIfEmpty(database: EnglishProDatabase) {
            val profileDao = database.userProfileDao()
            if (profileDao.getProfileOnce() == null) {
                profileDao.upsertProfile(UserProfileEntity())
            }

            val vocabDao = database.vocabDao()
            val fsrsDao = database.fsrsDao()
            if (vocabDao.getAllVocabOnce().isEmpty()) {
                val now = System.currentTimeMillis()
                val starterWords = StarterVocabularySeed.getStarterWords(now)
                vocabDao.insertAll(starterWords)

                // Pre-populate FSRS cards: first 15 words in initial review queue so user can immediately practice SRS,
                // remaining words in State=0 (New)
                val cards = starterWords.mapIndexed { index, word ->
                    if (index < 15) {
                        FsrsCardEntity(
                            wordId = word.id,
                            stability = 2.0,
                            difficulty = 5.0,
                            state = 2,
                            reps = 1,
                            dueAtEpochMs = now - (15 - index) * 3_600_000L, // due now
                            lastReviewAtEpochMs = now - 2 * 86_400_000L,
                            recognitionPassCount = if (index % 3 == 0) 2 else 1,
                            productionPassCount = 0
                        )
                    } else {
                        FsrsCardEntity(wordId = word.id, state = 0)
                    }
                }
                fsrsDao.upsertCards(cards)
            }

            val packageDao = database.packageDao()
            packageDao.upsertPackages(StarterLessonsAndScenariosSeed.getSelectablePackages())

            val lessonDao = database.lessonAndRolePlayDao()
            lessonDao.upsertLessons(StarterLessonsAndScenariosSeed.getStarterLessons())
            lessonDao.upsertScenarios(StarterLessonsAndScenariosSeed.getRolePlayScenarios())

            val analyticsDao = database.analyticsDao()
            if (analyticsDao.getAllErrorPatternsOnce().isEmpty()) {
                analyticsDao.upsertErrorPatterns(StarterLessonsAndScenariosSeed.getInitialErrorPatterns())
            }
        }
    }
}
