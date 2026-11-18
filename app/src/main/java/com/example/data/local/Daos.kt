package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)
}

@Dao
interface VocabDao {
    @Query("SELECT * FROM vocab_words ORDER BY frequencyRank ASC")
    fun observeAllVocab(): Flow<List<VocabWordEntity>>

    @Query("SELECT * FROM vocab_words ORDER BY frequencyRank ASC")
    suspend fun getAllVocabOnce(): List<VocabWordEntity>

    @Query("SELECT * FROM vocab_words WHERE id = :id")
    suspend fun getVocabById(id: String): VocabWordEntity?

    @Query(
        """
        SELECT * FROM vocab_words
        WHERE (:query = '' OR word LIKE '%' || :query || '%' OR persianMeaning LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%')
        AND (:category = 'ALL' OR category = :category)
        AND (:cefr = 'ALL' OR cefrLevel = :cefr)
        ORDER BY frequencyRank ASC
        """
    )
    fun searchVocab(query: String, category: String, cefr: String): Flow<List<VocabWordEntity>>

    @Query("SELECT COUNT(*) FROM vocab_words")
    fun observeVocabCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocab_words WHERE isMasteredActive = 1")
    fun observeActiveMasteredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocab_words WHERE isPassiveOnly = 1")
    fun observePassiveOnlyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocab_words WHERE category != 'GENERAL' AND (isMasteredActive = 1 OR selfRating IN ('KNOWN', 'EASY'))")
    fun observeCivilMasteredCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<VocabWordEntity>)

    @Update
    suspend fun updateWord(word: VocabWordEntity)

    @Query("DELETE FROM vocab_words WHERE packageId = :packageId")
    suspend fun deleteByPackage(packageId: String)
}

@Dao
interface FsrsDao {
    @Query("SELECT * FROM fsrs_cards")
    fun observeAllCards(): Flow<List<FsrsCardEntity>>

    @Query("SELECT * FROM fsrs_cards")
    suspend fun getAllCardsOnce(): List<FsrsCardEntity>

    @Query("SELECT * FROM fsrs_cards WHERE wordId = :wordId")
    suspend fun getCard(wordId: String): FsrsCardEntity?

    @Query("SELECT * FROM fsrs_cards WHERE state > 0 AND dueAtEpochMs <= :nowMs ORDER BY dueAtEpochMs ASC")
    fun observeDueCards(nowMs: Long): Flow<List<FsrsCardEntity>>

    @Query("SELECT COUNT(*) FROM fsrs_cards WHERE state > 0 AND dueAtEpochMs <= :nowMs")
    fun observeDueCount(nowMs: Long): Flow<Int>

    @Query("SELECT * FROM fsrs_cards WHERE state = 0 ORDER BY wordId ASC LIMIT :limit")
    fun observeNewCards(limit: Int): Flow<List<FsrsCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCard(card: FsrsCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCards(cards: List<FsrsCardEntity>)

    @Insert
    suspend fun insertReviewLog(log: ReviewLogEntity)

    @Query("SELECT * FROM review_logs ORDER BY reviewedAtEpochMs DESC LIMIT 200")
    fun observeRecentLogs(): Flow<List<ReviewLogEntity>>

    @Query("SELECT * FROM review_logs")
    suspend fun getAllReviewLogsOnce(): List<ReviewLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLogs(logs: List<ReviewLogEntity>)

    @Transaction
    suspend fun recordReview(card: FsrsCardEntity, log: ReviewLogEntity, word: VocabWordEntity, vocabDao: VocabDao) {
        upsertCard(card)
        insertReviewLog(log)
        vocabDao.updateWord(word)
    }
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation_sessions ORDER BY startedAtEpochMs DESC")
    fun observeSessions(): Flow<List<ConversationSessionEntity>>

    @Query("SELECT * FROM conversation_sessions ORDER BY startedAtEpochMs DESC")
    suspend fun getAllSessionsOnce(): List<ConversationSessionEntity>

    @Query("SELECT * FROM conversation_turns WHERE sessionId = :sessionId ORDER BY createdAtEpochMs ASC")
    fun observeTurnsForSession(sessionId: Long): Flow<List<ConversationTurnEntity>>

    @Query("SELECT * FROM conversation_turns")
    suspend fun getAllTurnsOnce(): List<ConversationTurnEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ConversationSessionEntity): Long

    @Update
    suspend fun updateSession(session: ConversationSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurn(turn: ConversationTurnEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ConversationSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurns(turns: List<ConversationTurnEntity>)
}

@Dao
interface RecordingDao {
    @Query("SELECT * FROM speaking_recordings ORDER BY createdAtEpochMs DESC")
    fun observeRecordings(): Flow<List<SpeakingRecordingEntity>>

    @Query("SELECT * FROM speaking_recordings ORDER BY createdAtEpochMs DESC")
    suspend fun getAllRecordingsOnce(): List<SpeakingRecordingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: SpeakingRecordingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordings(recordings: List<SpeakingRecordingEntity>)

    @Update
    suspend fun updateRecording(recording: SpeakingRecordingEntity)

    @Query("DELETE FROM speaking_recordings WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface PackageDao {
    @Query("SELECT * FROM content_packages ORDER BY id ASC")
    fun observePackages(): Flow<List<ContentPackageEntity>>

    @Query("SELECT * FROM content_packages ORDER BY id ASC")
    suspend fun getAllPackagesOnce(): List<ContentPackageEntity>

    @Query("SELECT * FROM content_packages WHERE id = :id")
    suspend fun getPackage(id: String): ContentPackageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPackages(packages: List<ContentPackageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPackage(pkg: ContentPackageEntity)
}

@Dao
interface LessonAndRolePlayDao {
    @Query("SELECT * FROM lesson_modules ORDER BY id ASC")
    fun observeLessons(): Flow<List<LessonModuleEntity>>

    @Query("SELECT * FROM lesson_modules ORDER BY id ASC")
    suspend fun getAllLessonsOnce(): List<LessonModuleEntity>

    @Query("SELECT * FROM lesson_modules WHERE id = :id")
    suspend fun getLesson(id: String): LessonModuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessons(lessons: List<LessonModuleEntity>)

    @Update
    suspend fun updateLesson(lesson: LessonModuleEntity)

    @Query("DELETE FROM lesson_modules WHERE packageId = :packageId")
    suspend fun deleteLessonsByPackage(packageId: String)

    @Query("SELECT * FROM role_play_scenarios ORDER BY number ASC")
    fun observeScenarios(): Flow<List<RolePlayScenarioEntity>>

    @Query("SELECT * FROM role_play_scenarios ORDER BY number ASC")
    suspend fun getAllScenariosOnce(): List<RolePlayScenarioEntity>

    @Query("SELECT * FROM role_play_scenarios WHERE id = :id")
    suspend fun getScenario(id: String): RolePlayScenarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScenarios(scenarios: List<RolePlayScenarioEntity>)

    @Update
    suspend fun updateScenario(scenario: RolePlayScenarioEntity)
}

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM assessment_records ORDER BY takenAtEpochMs DESC")
    fun observeAssessments(): Flow<List<AssessmentRecordEntity>>

    @Query("SELECT * FROM assessment_records ORDER BY takenAtEpochMs DESC")
    suspend fun getAllAssessmentsOnce(): List<AssessmentRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(record: AssessmentRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessments(records: List<AssessmentRecordEntity>)

    @Query("SELECT * FROM study_logs ORDER BY dateIso DESC LIMIT 30")
    fun observeRecentStudyLogs(): Flow<List<StudyLogEntity>>

    @Query("SELECT * FROM study_logs ORDER BY dateIso DESC")
    suspend fun getAllStudyLogsOnce(): List<StudyLogEntity>

    @Query("SELECT * FROM study_logs WHERE dateIso = :dateIso")
    suspend fun getStudyLog(dateIso: String): StudyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyLog(log: StudyLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyLogs(logs: List<StudyLogEntity>)

    @Query("SELECT * FROM grammar_error_patterns ORDER BY count DESC")
    fun observeErrorPatterns(): Flow<List<GrammarErrorPatternEntity>>

    @Query("SELECT * FROM grammar_error_patterns")
    suspend fun getAllErrorPatternsOnce(): List<GrammarErrorPatternEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertErrorPattern(pattern: GrammarErrorPatternEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertErrorPatterns(patterns: List<GrammarErrorPatternEntity>)
}
