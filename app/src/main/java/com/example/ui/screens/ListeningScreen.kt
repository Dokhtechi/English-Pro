package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.*
import com.example.speech.AudioRecorderManager
import com.example.ui.components.CefrBadge
import com.example.ui.components.EnglishTextLtr
import com.example.ui.components.LtrBlock
import com.example.ui.components.VibrantAccentCard
import com.example.ui.localization.UiStrings
import com.example.ui.theme.*
import kotlinx.serialization.json.Json

private val listenJson = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListeningScreen(
    lessons: List<LessonModuleEntity>,
    recordings: List<SpeakingRecordingEntity>,
    audioRecorder: AudioRecorderManager,
    strings: UiStrings,
    onPlayTts: (String, String, Float) -> Unit,
    onSaveShadowingRecording: (String, String, Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLesson by remember(lessons) { mutableStateOf(lessons.firstOrNull { it.category.startsWith("LISTENING") } ?: lessons.firstOrNull()) }
    var revealTranscript by remember(selectedLesson?.id) { mutableStateOf(false) }
    var selectedSpeed by remember { mutableFloatStateOf(1.0f) }
    var lastUserRecordingPath by remember { mutableStateOf<String?>(null) }
    val isRecording by audioRecorder.isRecording.collectAsState()
    val isPlaying by audioRecorder.isPlaying.collectAsState()
    val selectedQuiz = remember(selectedLesson?.id) { mutableStateMapOf<Int, Int>() }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) audioRecorder.startRecording("shadowing")
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("listening_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = strings.listeningTab,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (strings.isPersian)
                    "۱. ابتدا بدون متن گوش دهید  ۲. به سوالات پاسخ دهید  ۳. متن را باز کنید و با سرعت 0.65x / 0.8x / 1.0x سایه‌زنی کنید."
                else
                    "1. Listen without transcript  2. Answer quiz  3. Reveal transcript & shadow sentence-by-sentence.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Speed chips using FlowRow
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1.0f to "1.0x", 0.8f to "0.8x", 0.65f to "0.65x").forEach { (spd, lbl) ->
                    FilterChip(
                        selected = selectedSpeed == spd,
                        onClick = { selectedSpeed = spd },
                        label = { Text(lbl, maxLines = 1, softWrap = false) }
                    )
                }
            }
        }

        items(lessons.take(5), key = { it.id }) { lesson ->
            FilterChip(
                selected = selectedLesson?.id == lesson.id,
                onClick = { selectedLesson = lesson },
                label = { Text("${lesson.accent} • ${lesson.title}", maxLines = 1) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        val current = selectedLesson
        if (current != null) {
            val dialogue = runCatching { listenJson.decodeFromString<List<LessonDialogueTurn>>(current.dialogueTurnsJson) }.getOrDefault(emptyList())
            val fullScript = dialogue.joinToString(" ") { it.text }
            val questions = runCatching { listenJson.decodeFromString<List<ComprehensionQuestion>>(current.comprehensionQuestionsJson) }.getOrDefault(emptyList())

            item {
                VibrantAccentCard(accentColor = VibrantRoyalBlue, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CefrBadge("${current.accent} • ${current.difficultyCefr}", accentColor = VibrantRoyalBlue)
                            TextButton(onClick = { revealTranscript = !revealTranscript }) {
                                Text(if (revealTranscript) strings.hideTranscript else strings.showTranscript, maxLines = 1, softWrap = false)
                            }
                        }
                        Button(
                            onClick = { onPlayTts(fullScript, current.accent, selectedSpeed) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("play_listening_full")
                        ) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (strings.isPersian) "پخش کل فایل شنیداری (${selectedSpeed}x)" else "Play Full Audio (${selectedSpeed}x)", maxLines = 1, softWrap = false)
                        }
                    }
                }
            }

            items(questions.indices.toList()) { idx ->
                val q = questions[idx]
                VibrantAccentCard(accentColor = VioletAccent, modifier = Modifier.fillMaxWidth()) {
                    LtrBlock {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            EnglishTextLtr(q.question, fontWeight = FontWeight.SemiBold, color = VioletAccent)
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
                Text(
                    text = if (strings.isPersian) "تمرین سایه‌زنی جمله‌به‌جمله (Shadowing):" else "Sentence-by-Sentence Shadowing:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(dialogue) { turn ->
                VibrantAccentCard(accentColor = VibrantTeal, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EnglishTextLtr("${turn.speaker} (${turn.accent})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = VibrantTeal, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onPlayTts(turn.text, turn.accent, selectedSpeed) }) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = "Play original sentence", tint = VibrantTeal)
                            }
                        }
                        if (revealTranscript) {
                            EnglishTextLtr(turn.text)
                            if (turn.persianHint.isNotBlank()) Text(turn.persianHint, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text(if (strings.isPersian) "متن مخفی است — گوش دهید و تکرار کنید" else "Transcript hidden — listen and shadow", style = MaterialTheme.typography.bodySmall)
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    if (isRecording) {
                                        val res = audioRecorder.stopRecordingSafely()
                                        if (res != null) {
                                            lastUserRecordingPath = res.filePath
                                            onSaveShadowingRecording(current.title, res.filePath, res.durationMs, turn.text)
                                        }
                                    } else {
                                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                        if (granted) audioRecorder.startRecording("shadowing")
                                        else micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier.heightIn(min = 48.dp).testTag("shadow_record_button")
                            ) {
                                Icon(if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(if (isRecording) "Stop" else "Record", maxLines = 1, softWrap = false)
                            }
                            if (lastUserRecordingPath != null) {
                                OutlinedButton(
                                    onClick = {
                                        if (isPlaying) audioRecorder.stopPlayback()
                                        else audioRecorder.playRecording(lastUserRecordingPath!!, 1.0f)
                                    },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isPlaying) "Stop" else "Play Yours", maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, VibrantAmber.copy(alpha = 0.5f)),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (strings.isPersian)
                            "توجه شفاف: مقایسه صوتی صدای شما با گوینده اصلی برای خودارزیابی تلفظ است؛ تشخیص گفتار (ASR) جایگزین امتیازدهی دقیق آوایی (Phonetic) نیست."
                        else
                            "Transparency note: Side-by-side playback lets you compare rhythm & stress directly. Speech recognition accuracy is not claimed as exact phonetic scoring.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (recordings.isNotEmpty()) {
            item {
                Text(
                    text = if (strings.isPersian) "ضبط‌های ذخیره شده محلی (${recordings.size}):" else "Saved Local Recordings (${recordings.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            items(recordings.take(5)) { rec ->
                VibrantAccentCard(accentColor = EmeraldSuccess, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            EnglishTextLtr(rec.title, fontWeight = FontWeight.SemiBold)
                            EnglishTextLtr("${rec.contextType} • ${rec.durationMs / 1000}s", style = MaterialTheme.typography.labelSmall, monospace = true)
                        }
                        IconButton(onClick = { audioRecorder.playRecording(rec.filePath, 1.0f) }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play saved recording", tint = EmeraldSuccess)
                        }
                    }
                }
            }
        }
    }
}
