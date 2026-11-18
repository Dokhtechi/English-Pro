package com.example.domain.srs

import com.example.data.local.FsrsCardEntity
import com.example.data.local.VocabWordEntity
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * FSRS-4.5 compatible Spaced Repetition Scheduler.
 * Distinguishes recognition vs active production and avoids repeating only easy words.
 */
object FsrsScheduler {
    private const val DAY_MS = 86_400_000L
    private const val MINUTE_MS = 60_000L

    // FSRS-4.5 default weights
    private val w = doubleArrayOf(
        0.4, 0.6, 2.4, 5.8,
        4.93, 0.94, 0.86, 0.01,
        1.49, 0.14, 0.94,
        2.18, 0.05, 0.34, 1.26,
        0.29, 2.61
    )

    private const val REQUEST_RETENTION = 0.90

    enum class Rating(val value: Int) {
        AGAIN(1),
        HARD(2),
        GOOD(3),
        EASY(4)
    }

    /**
     * Retrievability R(t, S) = (1 + t / (9 * S))^-1
     */
    fun calculateRetrievability(card: FsrsCardEntity, nowMs: Long): Double {
        if (card.state == 0 || card.stability <= 0.0 || card.lastReviewAtEpochMs <= 0L) return 0.0
        val elapsedDays = max(0.0, (nowMs - card.lastReviewAtEpochMs).toDouble() / DAY_MS)
        return (1.0 + elapsedDays / (9.0 * card.stability)).pow(-1.0)
    }

    fun scheduleNext(
        card: FsrsCardEntity,
        word: VocabWordEntity,
        rating: Rating,
        mode: String, // "RECOGNITION" | "PRODUCTION" | "SPEAKING"
        responseTimeMs: Long,
        userSentence: String = "",
        nowMs: Long = System.currentTimeMillis()
    ): Pair<FsrsCardEntity, VocabWordEntity> {
        val elapsedDays = if (card.lastReviewAtEpochMs > 0L) {
            max(0, ((nowMs - card.lastReviewAtEpochMs) / DAY_MS).toInt())
        } else 0

        // Penalize sluggish recall (> 12 seconds) slightly by adjusting effective grade
        val slowRecall = responseTimeMs > 12_000L && rating == Rating.EASY
        val effectiveRating = if (slowRecall) Rating.GOOD else rating

        var newDifficulty = card.difficulty
        var newStability = card.stability
        var newState = card.state
        var newLapses = card.lapses
        val newReps = card.reps + 1

        if (card.state == 0) {
            // First exposure
            newStability = initialStability(effectiveRating)
            newDifficulty = initialDifficulty(effectiveRating)
            newState = if (effectiveRating == Rating.AGAIN) 1 else 2
            if (effectiveRating == Rating.AGAIN) newLapses += 1
        } else {
            val retrievability = calculateRetrievability(card, nowMs)
            newDifficulty = nextDifficulty(card.difficulty, effectiveRating)
            if (effectiveRating == Rating.AGAIN) {
                newStability = nextForgetStability(newDifficulty, card.stability, retrievability)
                newState = 3 // Relearning
                newLapses += 1
            } else {
                newStability = nextRecallStability(newDifficulty, card.stability, retrievability, effectiveRating)
                newState = 2 // Review
            }
        }

        val intervalDays: Int
        val dueAtMs: Long
        if (effectiveRating == Rating.AGAIN) {
            intervalDays = 0
            dueAtMs = nowMs + 10 * MINUTE_MS
        } else if (card.state == 0 && effectiveRating == Rating.HARD) {
            intervalDays = 1
            dueAtMs = nowMs + 12 * 60 * MINUTE_MS
        } else {
            val rawDays = nextInterval(newStability)
            intervalDays = max(1, rawDays)
            dueAtMs = nowMs + intervalDays * DAY_MS
        }

        val newAvgTime = if (card.avgResponseTimeMs == 0L) responseTimeMs
        else (card.avgResponseTimeMs * 0.7 + responseTimeMs * 0.3).toLong()

        val passed = effectiveRating != Rating.AGAIN
        val newRecPass = card.recognitionPassCount + if (mode == "RECOGNITION" && passed) 1 else 0
        val newProdPass = card.productionPassCount + if ((mode == "PRODUCTION" || mode == "SPEAKING") && passed) 1 else 0

        // Passive vs Active classification:
        // If recognized 2+ times without active production pass, flag as passive-only!
        val isPassiveOnly = newRecPass >= 2 && newProdPass == 0
        val isMasteredActive = newProdPass >= 2 && newStability >= 7.0 && newLapses <= 1

        val updatedCard = card.copy(
            stability = newStability,
            difficulty = newDifficulty,
            elapsedDays = elapsedDays,
            scheduledDays = intervalDays,
            reps = newReps,
            lapses = newLapses,
            state = newState,
            dueAtEpochMs = dueAtMs,
            lastReviewAtEpochMs = nowMs,
            avgResponseTimeMs = newAvgTime,
            recognitionPassCount = newRecPass,
            productionPassCount = newProdPass,
            lastUserSentence = if (userSentence.isNotBlank()) userSentence else card.lastUserSentence
        )

        val updatedRating = when {
            effectiveRating == Rating.AGAIN -> "DIFFICULT"
            isMasteredActive -> "KNOWN"
            newRecPass >= 1 -> "PARTIAL"
            else -> word.selfRating
        }

        val updatedWord = word.copy(
            selfRating = updatedRating,
            isPassiveOnly = isPassiveOnly,
            isMasteredActive = isMasteredActive
        )

        return updatedCard to updatedWord
    }

    /**
     * Checks whether the learner has an excessive overdue review backlog.
     * Requirement: Do not introduce more new words when the learner has a large overdue backlog.
     */
    fun shouldGateNewVocabulary(dueCount: Int, threshold: Int = 25): Boolean {
        return dueCount >= threshold
    }

    private fun initialStability(rating: Rating): Double = w[rating.value - 1].coerceAtLeast(0.1)

    private fun initialDifficulty(rating: Rating): Double {
        return (w[4] - (rating.value - 3) * w[5]).coerceIn(1.0, 10.0)
    }

    private fun nextDifficulty(d: Double, rating: Rating): Double {
        val nextD = d - w[6] * (rating.value - 3)
        return (w[7] * initialDifficulty(Rating.GOOD) + (1.0 - w[7]) * nextD).coerceIn(1.0, 10.0)
    }

    private fun nextRecallStability(d: Double, s: Double, r: Double, rating: Rating): Double {
        val hardPenalty = if (rating == Rating.HARD) w[15] else 1.0
        val easyBonus = if (rating == Rating.EASY) w[16] else 1.0
        val factor = exp(w[8]) * (11.0 - d) * s.pow(-w[9]) * (exp((1.0 - r) * w[10]) - 1.0) * hardPenalty * easyBonus
        return (s * (1.0 + factor)).coerceIn(0.1, 3650.0)
    }

    private fun nextForgetStability(d: Double, s: Double, r: Double): Double {
        return (w[11] * d.pow(-w[12]) * ((s + 1.0).pow(w[13]) - 1.0) * exp((1.0 - r) * w[14]))
            .coerceIn(0.1, min(s, 30.0))
    }

    private fun nextInterval(s: Double): Int {
        val interval = 9.0 * s * (1.0 / REQUEST_RETENTION - 1.0)
        return max(1, interval.roundToInt())
    }
}
