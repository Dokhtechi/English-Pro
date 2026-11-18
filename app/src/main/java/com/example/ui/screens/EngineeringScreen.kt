package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.ui.components.CefrBadge
import com.example.ui.components.EnglishTextLtr
import com.example.ui.components.LtrBlock
import com.example.ui.components.VibrantAccentCard
import com.example.ui.localization.UiStrings
import com.example.ui.theme.*
import kotlinx.serialization.json.Json

private val engJson = Json { ignoreUnknownKeys = true }

private data class UsUkPair(val ukTerm: String, val usTerm: String, val persian: String, val context: String)

private val usUkEngineeringGlossary = listOf(
    UsUkPair("Variation Order (VO) / Delay Damages", "Change Order (CO) / Liquidated Damages", "دستور تغییر کار / خسارت تاخیر", "FIDIC vs US Contracts"),
    UsUkPair("Baseline Programme", "Baseline Schedule", "برنامه زمان‌بندی مبنا", "Project Controls"),
    UsUkPair("Shuttering / Formwork", "Formwork", "قالب‌بندی بتن", "Concrete Works"),
    UsUkPair("Blinding Concrete", "Mud Slab / Lean Concrete", "بتن مگر کف گود", "Foundations"),
    UsUkPair("Cover Blocks / Spacers", "Rebar Chairs / Dobies", "اسپیسر کاور میلگرد", "Reinforcement"),
    UsUkPair("Snag List", "Punch List", "فهرست نواقص تحویل", "Commissioning & Handover"),
    UsUkPair("Banksman", "Signalperson / Rigger", "نفر هدایت‌کننده باربرداری جرثقیل", "HSE Lifting"),
    UsUkPair("Cube Test (150 mm, BS EN)", "Cylinder Test (150x300 mm, ASTM)", "آزمایش مقاومت فشاری بتن", "QA/QC Testing"),
    UsUkPair("Inlet Works", "Headworks", "تاسیسات ورودی تصفیه‌خانه", "Wastewater Plants")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EngineeringScreen(
    lessons: List<LessonModuleEntity>,
    scenarios: List<RolePlayScenarioEntity>,
    strings: UiStrings,
    onPlayAudio: (String, String) -> Unit,
    onStartRolePlayInCoach: (RolePlayScenarioEntity) -> Unit,
    onMarkLessonComplete: (LessonModuleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeLesson by remember { mutableStateOf<LessonModuleEntity?>(null) }
    var activeOfflineScenario by remember { mutableStateOf<RolePlayScenarioEntity?>(null) }

    val lessonAccents = listOf(VibrantRoyalBlue, VibrantTeal, VioletAccent, VibrantAmber, EmeraldSuccess, CoralAccent)

    Column(modifier = modifier.fillMaxSize().testTag("engineering_screen")) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0; activeLesson = null; activeOfflineScenario = null },
                text = { Text(if (strings.isPersian) "درس‌های تخصصی (D1–D7)" else "Technical Lessons", maxLines = 1, softWrap = false) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1; activeLesson = null; activeOfflineScenario = null },
                text = { Text(if (strings.isPersian) "۲۰ نقش‌آفرینی کارگاهی" else "20 Role-Plays", maxLines = 1, softWrap = false) },
                modifier = Modifier.testTag("eng_roleplays_tab")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2; activeLesson = null; activeOfflineScenario = null },
                text = { Text(if (strings.isPersian) "تفاوت US / UK و FIDIC" else "US vs UK & FIDIC", maxLines = 1, softWrap = false) }
            )
        }

        when {
            activeLesson != null -> TechnicalLessonDetailView(
                lesson = activeLesson!!,
                strings = strings,
                onPlayAudio = onPlayAudio,
                onMarkComplete = {
                    onMarkLessonComplete(activeLesson!!)
                    activeLesson = null
                },
                onBack = { activeLesson = null }
            )
            activeOfflineScenario != null -> OfflineBranchingRolePlayView(
                scenario = activeOfflineScenario!!,
                strings = strings,
                onPlayAudio = onPlayAudio,
                onLaunchAiCoach = { onStartRolePlayInCoach(activeOfflineScenario!!) },
                onBack = { activeOfflineScenario = null }
            )
            selectedTab == 0 -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lessons.filter { !it.category.startsWith("LISTENING_") }, key = { it.id }) { lesson ->
                    val accent = lessonAccents[kotlin.math.abs(lesson.id.hashCode()) % lessonAccents.size]
                    VibrantAccentCard(
                        accentColor = accent,
                        onClick = { activeLesson = lesson },
                        modifier = Modifier.fillMaxWidth().testTag("lesson_card_${lesson.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CefrBadge("${lesson.category} • ${lesson.difficultyCefr}", accentColor = accent)
                                if (lesson.isCompleted) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Completed", tint = EmeraldSuccess)
                                }
                            }
                            EnglishTextLtr(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
                            Text(lesson.titleFa, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            selectedTab == 1 -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(scenarios, key = { it.id }) { scenario ->
                    val accent = lessonAccents[scenario.number % lessonAccents.size]
                    VibrantAccentCard(
                        accentColor = accent,
                        modifier = Modifier.fillMaxWidth().testTag("scenario_card_${scenario.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CefrBadge("#${scenario.number} • ${scenario.difficultyCefr}", accentColor = accent)
                                EnglishTextLtr("AI Role: ${scenario.aiRole}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                            EnglishTextLtr(scenario.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
                            Text(scenario.titleFa, style = MaterialTheme.typography.bodySmall)
                            EnglishTextLtr("Objective: ${scenario.objective}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { activeOfflineScenario = scenario },
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("offline_rp_${scenario.id}")
                                ) {
                                    Text(if (strings.isPersian) "تمرین آفلاین" else "Offline Branching", maxLines = 1, softWrap = false)
                                }
                                Button(
                                    onClick = { onStartRolePlayInCoach(scenario) },
                                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("voice_rp_${scenario.id}")
                                ) {
                                    Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (strings.isPersian) "شبیه‌ساز صوتی" else "Voice Sim", maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
            selectedTab == 2 -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(usUkEngineeringGlossary) { item ->
                    VibrantAccentCard(accentColor = VibrantTeal, modifier = Modifier.fillMaxWidth()) {
                        LtrBlock {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                CefrBadge(item.context, accentColor = VibrantAmber)
                                EnglishTextLtr("UK / FIDIC: ${item.ukTerm}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                EnglishTextLtr("US / ASTM: ${item.usTerm}", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                                Text(item.persian, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechnicalLessonDetailView(
    lesson: LessonModuleEntity,
    strings: UiStrings,
    onPlayAudio: (String, String) -> Unit,
    onMarkComplete: () -> Unit,
    onBack: () -> Unit
) {
    val sentences = remember(lesson.id) { runCatching { engJson.decodeFromString<List<VocabExample>>(lesson.naturalSentencesJson) }.getOrDefault(emptyList()) }
    val dialogue = remember(lesson.id) { runCatching { engJson.decodeFromString<List<LessonDialogueTurn>>(lesson.dialogueTurnsJson) }.getOrDefault(emptyList()) }
    val questions = remember(lesson.id) { runCatching { engJson.decodeFromString<List<ComprehensionQuestion>>(lesson.comprehensionQuestionsJson) }.getOrDefault(emptyList()) }
    val expressions = remember(lesson.id) { runCatching { engJson.decodeFromString<List<ExpressionNote>>(lesson.difficultExpressionsJson) }.getOrDefault(emptyList()) }
    val reviewQs = remember(lesson.id) { runCatching { engJson.decodeFromString<List<String>>(lesson.reviewQuestionsJson) }.getOrDefault(emptyList()) }
    val selectedQuiz = remember { mutableStateMapOf<Int, Int>() }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack) { Text(if (strings.isPersian) "← بازگشت به فهرست" else "← Back to Lessons", maxLines = 1, softWrap = false) }
            EnglishTextLtr(lesson.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(lesson.titleFa, style = MaterialTheme.typography.bodyMedium)
            EnglishTextLtr("US vs UK Note: ${lesson.britishVsAmericanNotes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
        item {
            Text(if (strings.isPersian) "جملات طبیعی و کاربردی:" else "Natural Sentences:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(sentences) { s ->
            VibrantAccentCard(accentColor = VibrantRoyalBlue, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        EnglishTextLtr(s.english)
                        Text(s.persian, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onPlayAudio(s.english, lesson.accent) }) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = "Play sentence", tint = VibrantRoyalBlue)
                    }
                }
            }
        }
        item {
            Text(if (strings.isPersian) "مکالمه تخصصی کارگاهی:" else "Professional Dialogue:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(dialogue) { turn ->
            VibrantAccentCard(accentColor = VibrantTeal, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        EnglishTextLtr("${turn.speaker} (${turn.accent})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = VibrantTeal, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onPlayAudio(turn.text, turn.accent) }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play turn", tint = VibrantTeal)
                        }
                    }
                    EnglishTextLtr(turn.text)
                    if (turn.persianHint.isNotBlank()) Text(turn.persianHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text(if (strings.isPersian) "اصطلاحات کلیدی:" else "Useful Expressions:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(expressions) { exp ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, VioletAccent.copy(alpha = 0.35f)),
                shape = MaterialTheme.shapes.small
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                    EnglishTextLtr("${exp.expression} — ${exp.meaningEn}", fontWeight = FontWeight.SemiBold, color = VioletAccent)
                    Text(exp.meaningFa, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Text(if (strings.isPersian) "سوالات درک مطلب:" else "Comprehension Check:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(questions.indices.toList()) { idx ->
            val q = questions[idx]
            VibrantAccentCard(accentColor = VioletAccent, modifier = Modifier.fillMaxWidth()) {
                LtrBlock {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        EnglishTextLtr(q.question, fontWeight = FontWeight.SemiBold)
                        q.options.forEachIndexed { oIdx, opt ->
                            val picked = selectedQuiz[idx] == oIdx
                            OutlinedButton(
                                onClick = { selectedQuiz[idx] = oIdx },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                colors = when {
                                    selectedQuiz[idx] == null -> ButtonDefaults.outlinedButtonColors()
                                    oIdx == q.correctIndex -> ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                    picked -> ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    else -> ButtonDefaults.outlinedButtonColors()
                                }
                            ) { Text(opt) }
                        }
                    }
                }
            }
        }
        item {
            VibrantAccentCard(accentColor = VibrantAmber, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (strings.isPersian) "تکلیف عملی مکالمه و مرور:" else "Practical Speaking Task & Review:", fontWeight = FontWeight.Bold, color = VibrantAmber)
                    EnglishTextLtr(lesson.practicalSpeakingTask)
                    reviewQs.forEach { rq -> EnglishTextLtr("• $rq", style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
        item {
            Button(
                onClick = onMarkComplete,
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
            ) {
                Text(if (strings.isPersian) "ثبت تکمیل درس (+۱۰ دقیقه)" else "Mark Lesson Completed (+10 min)", maxLines = 1, softWrap = false)
            }
        }
    }
}

@Composable
private fun OfflineBranchingRolePlayView(
    scenario: RolePlayScenarioEntity,
    strings: UiStrings,
    onPlayAudio: (String, String) -> Unit,
    onLaunchAiCoach: () -> Unit,
    onBack: () -> Unit
) {
    val nodes = remember(scenario.id) {
        runCatching { engJson.decodeFromString<List<OfflineBranchNode>>(scenario.offlineBranchingJson) }.getOrDefault(emptyList())
    }
    var stepIndex by remember(scenario.id) { mutableIntStateOf(0) }
    var pickedOption by remember(scenario.id, stepIndex) { mutableStateOf<String?>(null) }
    val currentNode = nodes.getOrNull(stepIndex)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack) { Text(if (strings.isPersian) "← بازگشت" else "← Back", maxLines = 1, softWrap = false) }
            EnglishTextLtr(scenario.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            EnglishTextLtr("Background: ${scenario.background}", style = MaterialTheme.typography.bodySmall)
            EnglishTextLtr("Complication: ${scenario.complicationPrompt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
        if (currentNode != null) {
            item {
                VibrantAccentCard(accentColor = VibrantTeal, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            CefrBadge(scenario.aiRole, accentColor = VibrantTeal)
                            IconButton(onClick = { onPlayAudio(currentNode.aiPrompt, "INT") }) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = "Play prompt", tint = VibrantTeal)
                            }
                        }
                        EnglishTextLtr(currentNode.aiPrompt, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(currentNode.coachHintFa, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)

                        currentNode.suggestedResponses.forEach { option ->
                            OutlinedButton(
                                onClick = { pickedOption = option },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) {
                                Text(option)
                            }
                        }
                        if (pickedOption != null) {
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                                EnglishTextLtr("Selected response: $pickedOption", modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
                            }
                            Button(
                                onClick = {
                                    if (stepIndex < nodes.lastIndex) stepIndex++ else onBack()
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) {
                                Text(if (stepIndex < nodes.lastIndex) "Next Turn" else "Finish Offline Role-Play", maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }
        }
        item {
            FilledTonalButton(onClick = onLaunchAiCoach, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(if (strings.isPersian) "ادامه با شبیه‌ساز صوتی کامل" else "Continue in Full Voice Coach", maxLines = 1, softWrap = false)
            }
        }
    }
}
