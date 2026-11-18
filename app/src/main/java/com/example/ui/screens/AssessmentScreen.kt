package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.AssessmentRecordEntity
import com.example.speech.AudioRecorderManager
import com.example.ui.components.CefrBadge
import com.example.ui.components.EnglishTextLtr
import com.example.ui.components.LtrBlock
import com.example.ui.components.VibrantAccentCard
import com.example.ui.localization.UiStrings
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class AssessmentMcq(
    val section: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val ttsSample: String? = null,
    val ttsAccent: String = "US"
)

private val diagnosticQuestions = listOf(
    AssessmentMcq("VOCAB", "Which verb best completes: 'We installed well-point pumps to ___ the risk of trench collapse'?", listOf("mitigate", "escalate", "compromise", "backfill"), 0),
    AssessmentMcq("VOCAB", "In a meeting, 'to iron out minor clashes' means:", listOf("Delay the pour", "Resolve small remaining disagreements", "Issue an NCR", "Cancel the contract"), 1),
    AssessmentMcq("VOCAB", "What is the FIDIC equivalent of an American 'Change Order'?", listOf("Snag list", "Variation Order", "Blinding", "Method statement"), 1),
    AssessmentMcq("GRAMMAR", "Choose the accurate sentence:", listOf("We need more equipments for compaction.", "If the pump hadn't tripped, the water table wouldn't have risen.", "I will follow up the RFI tomorrow.", "She suggested me to clarify the drawing."), 1),
    AssessmentMcq("GRAMMAR", "Select the natural conditional: '___ concrete reaches 75% strength, do not strip the slab props.'", listOf("Unless", "Despite", "Although", "Provided"), 0),
    AssessmentMcq("GRAMMAR", "Which is grammatically correct for site supervision?", listOf("The mix complies to ASTM.", "The mix complies with ASTM.", "The mix is comply with ASTM.", "The mix complied against ASTM."), 1),
    AssessmentMcq("LISTENING", "Listen to the US Resident Engineer clip and pick the true statement:", listOf("The pour starts immediately.", "Honeycombing was found on Grid C and needs a repair method statement.", "Slump was 50 mm."), 1, ttsSample = "After stripping the wall formwork on Grid C, we found honeycombing near the kicker. Stop any mortar patching and submit a structural repair method statement.", ttsAccent = "US"),
    AssessmentMcq("LISTENING", "Listen to the UK Project Manager clip and answer:", listOf("The culvert is on the critical path.", "The culvert has 18 days of total float so no Extension of Time applies.", "A variation order was approved."), 1, ttsSample = "Looking at the baseline programme, that culvert activity still has eighteen days of total float, so it doesn't sit on the critical path and no Extension of Time applies.", ttsAccent = "UK"),
    AssessmentMcq("PRONUNCIATION", "Where is the primary stress in 'sub-stan-ti-ate' (to prove a claim with evidence)?", listOf("SUB-stantiate", "sub-STAN-ti-ate", "sub-stan-ti-ATE", "equal stress"), 1),
    AssessmentMcq("PRONUNCIATION", "Which word has a silent 'b'?", listOf("rebar", "honeycombing", "backfill", "submersible"), 1)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AssessmentScreen(
    history: List<AssessmentRecordEntity>,
    audioRecorder: AudioRecorderManager,
    strings: UiStrings,
    onPlayTts: (String, String) -> Unit,
    onSaveAssessment: (Int, Int, Int, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val answers = remember { mutableStateMapOf<Int, Int>() }
    var speakingSampleText by remember { mutableStateOf("") }
    var speakingRecordedSeconds by remember { mutableStateOf(0) }
    var pronunciationConfidence by remember { mutableStateOf(3) }
    val isRecording by audioRecorder.isRecording.collectAsState()

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            audioRecorder.startRecording("assessment")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (strings.isPersian) "آزمون جامع تعیین سطح (CEFR)" else "Comprehensive Placement Assessment",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("assessment_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                VibrantAccentCard(accentColor = VibrantRoyalBlue, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (strings.isPersian)
                                "ارزیابی پنج مهارت: واژگان، گرامر، شنیداری (آمریکایی و بریتانیایی)، مکالمه و تلفظ"
                            else
                                "5-Skill Diagnostic: Vocabulary, Grammar, Listening (US/UK), Speaking & Pronunciation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VibrantRoyalBlue
                        )
                        Text(
                            text = if (strings.isPersian)
                                "توجه: خروجی یک برآورد آموزشی CEFR است و جایگزین نمره رسمی آیلتس نیست. این آزمون هر ۳۰ روز قابل تکرار است."
                            else
                                "Note: Generates an educational CEFR profile & unofficial practice band estimate. Repeatable every 30 days.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(diagnosticQuestions.indices.toList()) { idx ->
                val q = diagnosticQuestions[idx]
                val accent = when (q.section) {
                    "VOCAB" -> EmeraldSuccess
                    "GRAMMAR" -> VibrantRoyalBlue
                    "LISTENING" -> VibrantTeal
                    else -> VioletAccent
                }
                VibrantAccentCard(accentColor = accent, modifier = Modifier.fillMaxWidth()) {
                    LtrBlock {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CefrBadge(level = "${idx + 1}. ${q.section}", accentColor = accent)
                                if (q.ttsSample != null) {
                                    FilledTonalButton(
                                        onClick = { onPlayTts(q.ttsSample, q.ttsAccent) },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Filled.VolumeUp, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Play ${q.ttsAccent} Clip", maxLines = 1, softWrap = false)
                                    }
                                }
                            }
                            EnglishTextLtr(
                                text = q.prompt,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            q.options.forEachIndexed { optIdx, optText ->
                                val selected = answers[idx] == optIdx
                                OutlinedButton(
                                    onClick = { answers[idx] = optIdx },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("assess_q${idx}_opt$optIdx"),
                                    colors = if (selected) ButtonDefaults.outlinedButtonColors(
                                        containerColor = accent.copy(alpha = 0.2f)
                                    ) else ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text(optText, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }

            item {
                VibrantAccentCard(accentColor = VibrantAmber, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CefrBadge(level = "D. SPEAKING ASSESSMENT", accentColor = VibrantAmber)
                        EnglishTextLtr(
                            text = "Task: Introduce your 30 years of civil engineering experience and explain one technical site problem you resolved (e.g., dewatering, concrete quality, or contractor delay).",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isRecording) {
                                        val rec = audioRecorder.stopRecordingSafely()
                                        speakingRecordedSeconds = ((rec?.durationMs ?: 0L) / 1000L).toInt()
                                    } else {
                                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                        if (granted) audioRecorder.startRecording("assessment")
                                        else micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier.heightIn(min = 48.dp).testTag("assessment_record_btn")
                            ) {
                                Icon(if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(if (isRecording) "Stop (${speakingRecordedSeconds}s)" else "Record Spoken Answer", maxLines = 1, softWrap = false)
                            }
                            if (speakingRecordedSeconds > 0) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Saved ${speakingRecordedSeconds}s audio", maxLines = 1, softWrap = false) },
                                    leadingIcon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldSuccess) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = speakingSampleText,
                            onValueChange = { speakingSampleText = it },
                            modifier = Modifier.fillMaxWidth().testTag("assessment_speaking_input"),
                            label = { Text("Optional summary / transcript of your spoken answer") },
                            minLines = 3
                        )
                        Text(
                            text = if (strings.isPersian) "تسلط و وضوح تلفظ خود را از ۱ تا ۵ ارزیابی کنید:" else "Rate your current speaking confidence (1–5):",
                            style = MaterialTheme.typography.bodySmall
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (1..5).forEach { score ->
                                FilterChip(
                                    selected = pronunciationConfidence == score,
                                    onClick = { pronunciationConfidence = score },
                                    label = { Text("$score", maxLines = 1, softWrap = false) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        fun sectionPct(section: String): Int {
                            val items = diagnosticQuestions.mapIndexedNotNull { i, q -> if (q.section == section) i to q else null }
                            if (items.isEmpty()) return 60
                            val correct = items.count { (i, q) -> answers[i] == q.correctIndex }
                            return ((correct.toDouble() / items.size) * 100).toInt().coerceIn(35, 98)
                        }
                        val vocabPct = sectionPct("VOCAB")
                        val grammarPct = sectionPct("GRAMMAR")
                        val listenPct = sectionPct("LISTENING")
                        val pronBase = sectionPct("PRONUNCIATION")
                        val pronPct = ((pronBase * 0.6) + (pronunciationConfidence * 20 * 0.4)).toInt().coerceIn(35, 95)
                        val wordBonus = speakingSampleText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                        val speakingPct = (45 + (speakingRecordedSeconds.coerceAtMost(45)) + (wordBonus.coerceAtMost(25))).coerceIn(38, 92)
                        onSaveAssessment(vocabPct, grammarPct, listenPct, speakingPct, pronPct)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("submit_assessment_button")
                ) {
                    Text(if (strings.isPersian) "محاسبه و ذخیره پروفایل CEFR" else "Calculate & Save CEFR Profile", maxLines = 1, softWrap = false)
                }
            }

            if (history.isNotEmpty()) {
                item {
                    Text(
                        text = if (strings.isPersian) "تاریخچه ارزیابی‌ها (مقایسه ۳۰ روزه)" else "Assessment History (30-Day Comparison)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(history) { rec ->
                    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(rec.takenAtEpochMs))
                    VibrantAccentCard(accentColor = EmeraldSuccess, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CefrBadge(level = "Overall: ${rec.overallCefr}", accentColor = EmeraldSuccess)
                                EnglishTextLtr(text = dateStr, style = MaterialTheme.typography.labelSmall, monospace = true, maxLines = 1, softWrap = false)
                            }
                            EnglishTextLtr(
                                text = "Vocab ${rec.vocabScore}% • Grammar ${rec.grammarScore}% • Listen ${rec.listeningScore}% • Speak ${rec.speakingScore}% • Pron ${rec.pronunciationScore}%",
                                style = MaterialTheme.typography.bodySmall,
                                monospace = true
                            )
                            EnglishTextLtr(
                                text = rec.estimatedLearningNote,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
