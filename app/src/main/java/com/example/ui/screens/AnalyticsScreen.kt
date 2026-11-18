package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.ui.components.CefrBadge
import com.example.ui.components.EnglishTextLtr
import com.example.ui.components.VibrantAccentCard
import com.example.ui.localization.UiStrings
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    allVocab: List<VocabWordEntity>,
    activeMasteredCount: Int,
    passiveOnlyCount: Int,
    civilMasteredCount: Int,
    reviewLogs: List<ReviewLogEntity>,
    sessions: List<ConversationSessionEntity>,
    scenarios: List<RolePlayScenarioEntity>,
    assessments: List<AssessmentRecordEntity>,
    studyLogs: List<StudyLogEntity>,
    errorPatterns: List<GrammarErrorPatternEntity>,
    strings: UiStrings,
    modifier: Modifier = Modifier
) {
    val totalVocab = allVocab.size
    val retentionRate = if (reviewLogs.isEmpty()) 0 else {
        (reviewLogs.count { it.rating >= 3 } * 100) / reviewLogs.size
    }
    val avgRecallMs = if (reviewLogs.isEmpty()) 0L else reviewLogs.map { it.responseTimeMs }.average().toLong()
    val totalSpeakingMin = sessions.sumOf { it.durationSeconds } / 60
    val completedSessions = sessions.filter { it.endedAtEpochMs > 0L }
    val avgFluency = if (completedSessions.isEmpty()) 0 else completedSessions.map { it.fluencyScore }.average().toInt()
    val avgGrammar = if (completedSessions.isEmpty()) 0 else completedSessions.map { it.grammarScore }.average().toInt()
    val avgPron = if (completedSessions.isEmpty()) 0 else completedSessions.map { it.pronunciationScore }.average().toInt()
    val completedRolePlays = scenarios.count { it.completedCount > 0 }

    val latestAssess = assessments.firstOrNull()
    val firstAssess = assessments.lastOrNull()

    val skillRanking = listOf(
        Triple(if (strings.isPersian) "روانی مکالمه" else "Speaking Fluency", latestAssess?.speakingScore ?: avgFluency, "Practice 20-min Voice Coach in Fluency Mode"),
        Triple(if (strings.isPersian) "دقت گرامری" else "Grammar Accuracy", latestAssess?.grammarScore ?: avgGrammar, "Switch Coach to Accuracy Mode & review error patterns"),
        Triple(if (strings.isPersian) "تلفظ و وضوح" else "Pronunciation Intelligibility", latestAssess?.pronunciationScore ?: avgPron, "Complete 10-min 0.8x Shadowing drills"),
        Triple(if (strings.isPersian) "درک شنیداری" else "Listening Comprehension", latestAssess?.listeningScore ?: 65, "Do blind listening exercises before opening transcript"),
        Triple(if (strings.isPersian) "واژگان فعال" else "Active Vocabulary", if (totalVocab == 0) 0 else (activeMasteredCount * 100 / totalVocab), "Convert passive words via sentence production")
    ).sortedBy { it.second }
    val weakestThree = skillRanking.take(3)

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(strings.analyticsTab, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        item {
            VibrantAccentCard(accentColor = VibrantRoyalBlue, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (strings.isPersian) "شاخص‌های مستقل پیشرفت" else "Independent Performance Metrics", fontWeight = FontWeight.Bold, color = VibrantRoyalBlue)
                    MetricRow("Total / Active / Passive Vocab", "$totalVocab / $activeMasteredCount / $passiveOnlyCount")
                    MetricRow("FSRS Retention Rate", "$retentionRate%")
                    MetricRow("Avg Recall Speed", "${avgRecallMs / 1000.0}s")
                    MetricRow("Total Speaking Duration", "$totalSpeakingMin min")
                    MetricRow("Fluency / Grammar / Intelligibility", "$avgFluency% / $avgGrammar% / $avgPron%")
                    MetricRow("Civil Engineering Vocab Mastered", "$civilMasteredCount")
                    MetricRow("Completed Role-Play Scenarios", "$completedRolePlays / ${scenarios.size}")
                }
            }
        }

        item {
            VibrantAccentCard(accentColor = VibrantTeal, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (strings.isPersian) "نمودار دقایق مطالعه (اخیر)" else "Recent Study Minutes Chart", fontWeight = FontWeight.Bold, color = VibrantTeal)
                    val chartBars = studyLogs.take(14).reversed()
                    val primaryColor = VibrantTeal
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    if (chartBars.isEmpty()) {
                        Text(if (strings.isPersian) "هنوز سابقه مطالعه ثبت نشده است." else "Complete your first review or speaking turn to populate chart.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            val maxVal = (chartBars.maxOfOrNull { it.totalMinutes } ?: 60).coerceAtLeast(30).toFloat()
                            val count = chartBars.size
                            val gap = 8.dp.toPx()
                            val barW = ((size.width - gap * (count - 1)) / count).coerceAtLeast(8f)
                            chartBars.forEachIndexed { idx, log ->
                                val x = idx * (barW + gap)
                                drawRoundRect(
                                    color = trackColor,
                                    topLeft = Offset(x, 0f),
                                    size = Size(barW, size.height),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                                val h = (log.totalMinutes / maxVal) * size.height
                                drawRoundRect(
                                    color = primaryColor,
                                    topLeft = Offset(x, size.height - h),
                                    size = Size(barW, h),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (latestAssess != null && firstAssess != null) {
            item {
                VibrantAccentCard(accentColor = EmeraldSuccess, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (strings.isPersian) "مقایسه با ارزیابی اولیه" else "Progress vs Initial Assessment", fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CefrBadge("Initial: ${firstAssess.overallCefr}", accentColor = VibrantRoyalBlue)
                            CefrBadge("Latest: ${latestAssess.overallCefr}", accentColor = EmeraldSuccess)
                        }
                        EnglishTextLtr(
                            "Speaking: ${firstAssess.speakingScore}% → ${latestAssess.speakingScore}%  |  Vocab: ${firstAssess.vocabScore}% → ${latestAssess.vocabScore}%",
                            style = MaterialTheme.typography.bodySmall,
                            monospace = true
                        )
                    }
                }
            }
        }

        item {
            VibrantAccentCard(accentColor = VibrantAmber, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (strings.isPersian) "۳ مهارت ضعیف‌تر و پیشنهاد تمرینی" else "Top 3 Focus Areas & Recommendations", fontWeight = FontWeight.Bold, color = VibrantAmber)
                    weakestThree.forEach { (skill, score, action) ->
                        Column {
                            Text("$skill ($score%)", fontWeight = FontWeight.SemiBold)
                            EnglishTextLtr("→ $action", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (errorPatterns.isNotEmpty()) {
            item {
                Text(if (strings.isPersian) "الگوهای خطای پرتکرار (حافظه شخصی‌سازی)" else "Recurring Error Patterns (Coach Memory)", fontWeight = FontWeight.Bold)
            }
            items(errorPatterns) { err ->
                VibrantAccentCard(accentColor = CoralAccent, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        EnglishTextLtr("${err.patternLabel} (${err.count}x)", fontWeight = FontWeight.SemiBold, color = CoralAccent)
                        EnglishTextLtr(err.lastExample, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        EnglishTextLtr(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        EnglishTextLtr(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            monospace = true,
            maxLines = 1,
            softWrap = false
        )
    }
}
