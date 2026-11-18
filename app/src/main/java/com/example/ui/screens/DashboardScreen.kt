package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.StarterLessonsAndScenariosSeed
import com.example.data.local.StudyLogEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.CefrBadge
import com.example.ui.components.CefrLevelGuideDialog
import com.example.ui.components.EnglishTextLtr
import com.example.ui.components.OnlineOfflineStatusBadge
import com.example.ui.components.VibrantAccentCard
import com.example.ui.localization.UiStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.DailyPlanBreakdown
import java.util.Calendar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    profile: UserProfileEntity,
    todayLog: StudyLogEntity?,
    dueCount: Int,
    vocabCount: Int,
    activeMasteredCount: Int,
    dailyPlan: DailyPlanBreakdown,
    isOnline: Boolean,
    strings: UiStrings,
    onNavigateToSpeaking: () -> Unit,
    onNavigateToVocab: () -> Unit,
    onNavigateToEngineering: () -> Unit,
    onNavigateToListening: () -> Unit,
    onNavigateToAssessment: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToTools: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleThemeMode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayCalDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val studiedMin = todayLog?.totalMinutes ?: 0
    val targetMin = dailyPlan.totalTargetMinutes.coerceAtLeast(1)
    val progress = (studiedMin.toFloat() / targetMin.toFloat()).coerceIn(0f, 1f)
    val systemDark = isSystemInDarkTheme()
    val isDarkNow = when (profile.darkModePreference) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }
    var showCefrGuideDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header: Theme Toggle at Top-Left, App Name Centered, Settings Icon at Top-Right
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.ui.components.LtrBlock(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Physical Top-Left: Light/Dark Theme toggle
                        FilledTonalIconButton(
                            onClick = { onToggleThemeMode(if (isDarkNow) "LIGHT" else "DARK") },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.tertiary
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("dashboard_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkNow) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = if (strings.isPersian) "تغییر تم روشن و تاریک" else "Toggle Light/Dark Theme"
                            )
                        }

                        // Centered App Name (symmetrical 48dp buttons on both sides)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            EnglishTextLtr(
                                text = strings.appTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Physical Top-Right: Settings Icon Button
                        FilledTonalIconButton(
                            onClick = onOpenSettings,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("dashboard_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = if (strings.isPersian) "تنظیمات برنامه" else "App Settings"
                            )
                        }
                    }
                }

                Text(
                    text = if (strings.isPersian) "مربی شخصی انگلیسی و مهندسی عمران" else "Personal English & Civil Engineering Coach",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Repositioned Online/Gemini Status Badge neatly centered below App Name
                OnlineOfflineStatusBadge(
                    isOnline = isOnline,
                    onlineText = strings.onlineLabel,
                    offlineText = strings.offlineLabel
                )
            }
        }

        // Vibrant Hero Blueprint Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                VibrantRoyalBlue,
                                VioletAccent,
                                VibrantTeal
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(
                                text = strings.todayProgress,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.90f)
                            )
                            Text(
                                text = "$studiedMin / $targetMin min",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.20f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = strings.currentCefr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    softWrap = false
                                )
                                EnglishTextLtr(
                                    text = "${profile.overallCefr} → ${profile.targetLevel}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    monospace = true,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = LuminousGold,
                        trackColor = Color.White.copy(alpha = 0.24f)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToSpeaking,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("quick_speak_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = VibrantRoyalBlue
                            )
                        ) {
                            Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.quickSpeakBtn, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick = onNavigateToVocab,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("quick_flashcards_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White.copy(alpha = 0.22f),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f))
                        ) {
                            Icon(Icons.Filled.Style, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.flashcardsTab, maxLines = 1, softWrap = false, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Assessment Prompt Banner
        item {
            VibrantAccentCard(
                accentColor = if (!profile.initialAssessmentCompleted) AmberWarning else VibrantTeal,
                modifier = Modifier.fillMaxWidth().testTag("assessment_banner_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (!profile.initialAssessmentCompleted) strings.startAssessmentBtn else strings.repeatAssessmentBtn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (strings.isPersian)
                                "تعیین سطح واقعی مکالمه، شنیداری، واژگان، گرامر و تلفظ (تکرار هر ۳۰ روز)"
                            else
                                "5-skill placement test (Speaking, Listening, Vocab, Grammar, Pronunciation)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = onNavigateToAssessment,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("open_assessment_button")
                    ) {
                        Text(if (strings.isPersian) "شروع" else "Start", maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        // 4 Colorful Metric Tiles Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    title = strings.dueReviews,
                    value = "$dueCount",
                    subtitle = if (strings.isPersian) "کارت سررسید" else "FSRS due",
                    icon = Icons.Filled.Schedule,
                    accentColor = CoralAccent,
                    modifier = Modifier.weight(1f).clickable { onNavigateToVocab() }
                )
                MetricTile(
                    title = strings.newWordsLearned,
                    value = "$activeMasteredCount / $vocabCount",
                    subtitle = if (strings.isPersian) "تولید فعال / کل" else "Active / Total",
                    icon = Icons.Filled.AutoStories,
                    accentColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f).clickable { onNavigateToVocab() }
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    title = strings.speakingMinutes,
                    value = "${todayLog?.speakingMinutes ?: 0}m",
                    subtitle = if (strings.isPersian) "هدف امروز: ${dailyPlan.speakingMinutesTarget} دقیقه" else "Target: ${dailyPlan.speakingMinutesTarget}m",
                    icon = Icons.Filled.RecordVoiceOver,
                    accentColor = VioletAccent,
                    modifier = Modifier.weight(1f).clickable { onNavigateToSpeaking() }
                )
                MetricTile(
                    title = strings.streakLabel,
                    value = "${profile.streakDays}d",
                    subtitle = if (strings.isPersian) "کل: ${profile.totalStudyMinutes} دقیقه" else "Total: ${profile.totalStudyMinutes}m",
                    icon = Icons.Filled.LocalFireDepartment,
                    accentColor = VibrantAmber,
                    modifier = Modifier.weight(1f).clickable { onNavigateToAnalytics() }
                )
            }
        }

        // Daily 60-Min Breakdown Card
        item {
            VibrantAccentCard(accentColor = VibrantRoyalBlue, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.dailyPlanTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (dailyPlan.isNewVocabGatedByBacklog) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = strings.backlogWarning,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    PlanRow(
                        label = if (strings.isPersian) "۱. مرور فاصله‌دار واژگان قبلی" else "1. Spaced Repetition Review",
                        done = todayLog?.srsMinutes ?: 0,
                        target = dailyPlan.srsMinutesTarget,
                        barColor = CoralAccent,
                        onClick = onNavigateToVocab
                    )
                    PlanRow(
                        label = if (strings.isPersian) "۲. یادگیری ۸–۱۰ واژه جدید" else "2. New Vocabulary (8–10 items)",
                        done = todayLog?.vocabMinutes ?: 0,
                        target = dailyPlan.newVocabMinutesTarget,
                        barColor = EmeraldSuccess,
                        onClick = onNavigateToVocab
                    )
                    PlanRow(
                        label = if (strings.isPersian) "۳. مکالمه صوتی و پاسخ بداهه" else "3. Voice-Based Speaking Practice",
                        done = todayLog?.speakingMinutes ?: 0,
                        target = dailyPlan.speakingMinutesTarget,
                        barColor = VioletAccent,
                        onClick = onNavigateToSpeaking
                    )
                    PlanRow(
                        label = if (strings.isPersian) "۴. شنیداری و سایه‌زنی (Shadowing)" else "4. Listening & Shadowing",
                        done = todayLog?.listeningMinutes ?: 0,
                        target = dailyPlan.listeningMinutesTarget,
                        barColor = VibrantTeal,
                        onClick = onNavigateToListening
                    )
                    PlanRow(
                        label = if (strings.isPersian) "۵. انگلیسی تخصصی عمران و بازخورد" else "5. Civil Engineering Practice & Review",
                        done = todayLog?.proMinutes ?: 0,
                        target = dailyPlan.professionalMinutesTarget,
                        barColor = VibrantAmber,
                        onClick = onNavigateToEngineering
                    )
                }
            }
        }

        // CEFR Skill Sub-Scores using FlowRow so badges never crush on small widths or large fonts
        item {
            VibrantAccentCard(accentColor = VibrantTeal, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (strings.isPersian) "پروفایل مهارت‌های پنج‌گانه" else "5-Skill CEFR Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showCefrGuideDialog = true },
                            modifier = Modifier.size(48.dp).testTag("dashboard_cefr_guide_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = if (strings.isPersian) "توضیح سطوح زبان" else "CEFR Level Guide",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SkillBadgeColumn(if (strings.isPersian) "مکالمه" else "Speak", profile.speakingCefr, VioletAccent)
                        SkillBadgeColumn(if (strings.isPersian) "شنیداری" else "Listen", profile.listeningCefr, VibrantTeal)
                        SkillBadgeColumn(if (strings.isPersian) "واژگان" else "Vocab", profile.vocabularyCefr, EmeraldSuccess)
                        SkillBadgeColumn(if (strings.isPersian) "گرامر" else "Gram", profile.grammarCefr, VibrantRoyalBlue)
                        SkillBadgeColumn(if (strings.isPersian) "تلفظ" else "Pron", profile.pronunciationCefr, VibrantAmber)
                    }
                }
            }
        }

        // Weekly Learning Schedule
        item {
            Text(
                text = strings.weeklyScheduleTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(StarterLessonsAndScenariosSeed.weeklySchedule) { day ->
            val isToday = day.dayOfWeekIndex == todayCalDay
            VibrantAccentCard(
                accentColor = if (isToday) VibrantRoyalBlue else VibrantTeal.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (strings.isPersian) day.dayNameFa else day.dayNameEn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (strings.isPersian) day.themeFa else day.themeEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isToday) {
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = {
                                when {
                                    day.recommendedModule.startsWith("SPEAKING") -> onNavigateToSpeaking()
                                    day.recommendedModule.startsWith("ENGINEERING") || day.recommendedModule.startsWith("ROLEPLAY") -> onNavigateToEngineering()
                                    else -> onNavigateToVocab()
                                }
                            },
                            label = { Text(if (strings.isPersian) "برنامه امروز" else "Today", maxLines = 1, softWrap = false) }
                        )
                    }
                }
            }
        }

        // Quick Tools Entry
        item {
            OutlinedButton(
                onClick = onNavigateToTools,
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
            ) {
                Icon(Icons.Filled.Tune, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (strings.isPersian)
                        "شنیداری، بسته‌های آفلاین، عیب‌یابی شیائومی و پشتیبان‌گیری"
                    else
                        "Listening, Offline Packages, Xiaomi Diagnostics & Backup"
                )
            }
        }
    }

    if (showCefrGuideDialog) {
        CefrLevelGuideDialog(
            isPersian = strings.isPersian,
            currentLevel = profile.overallCefr,
            targetLevel = profile.targetLevel,
            onDismiss = { showCefrGuideDialog = false }
        )
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    VibrantAccentCard(accentColor = accentColor, modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.18f)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.padding(4.dp).size(16.dp)
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            EnglishTextLtr(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                monospace = true,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun PlanRow(
    label: String,
    done: Int,
    target: Int,
    barColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            EnglishTextLtr(
                text = "$done / $target min",
                style = MaterialTheme.typography.labelMedium,
                color = barColor,
                fontWeight = FontWeight.Bold,
                monospace = true,
                maxLines = 1,
                softWrap = false
            )
        }
        LinearProgressIndicator(
            progress = { if (target <= 0) 1f else (done.toFloat() / target).coerceIn(0f, 1f) },
            color = barColor,
            trackColor = barColor.copy(alpha = 0.16f),
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun SkillBadgeColumn(label: String, cefr: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
        CefrBadge(level = cefr, accentColor = accentColor)
    }
}
