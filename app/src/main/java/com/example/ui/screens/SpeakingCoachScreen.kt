package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.local.ConversationTurnEntity
import com.example.data.local.TurnCorrection
import com.example.ui.components.EnglishTextLtr
import com.example.ui.components.LtrBlock
import com.example.ui.components.OnlineOfflineStatusBadge
import com.example.ui.components.VibrantAccentCard
import com.example.ui.localization.UiStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoachUiState
import com.example.ui.viewmodel.SpeakingViewModel
import kotlinx.serialization.json.Json

private val coachJson = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeakingCoachScreen(
    speakingViewModel: SpeakingViewModel,
    isOnline: Boolean,
    strings: UiStrings,
    modifier: Modifier = Modifier,
    voiceGender: String = "FEMALE",
    onSetVoiceGender: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coachState by speakingViewModel.coachState.collectAsState()
    val isCoachSpeaking by speakingViewModel.ttsManager.isSpeaking.collectAsState()
    val activeSession by speakingViewModel.activeSession.collectAsState()
    val activeScenario by speakingViewModel.activeScenario.collectAsState()
    val turns by speakingViewModel.turns.collectAsState()
    val pastSessions by speakingViewModel.pastSessions.collectAsState()
    val correctionMode by speakingViewModel.correctionMode.collectAsState()
    val difficulty by speakingViewModel.difficulty.collectAsState()
    val speechRate by speakingViewModel.speechRate.collectAsState()
    val accent by speakingViewModel.accent.collectAsState()
    val showTranscript by speakingViewModel.showTranscript.collectAsState()
    val draftInput by speakingViewModel.draftInput.collectAsState()
    val latestReport by speakingViewModel.latestReport.collectAsState()
    val helperFeedback by speakingViewModel.helperFeedback.collectAsState()

    var showHistorySheet by remember { mutableStateOf(false) }
    var isAvatarExpanded by remember { mutableStateOf(true) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speakingViewModel.startVoiceTurn()
        }
    }

    LaunchedEffect(Unit) {
        if (activeSession == null) {
            speakingViewModel.startFreeSession()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("speaking_coach_screen")
    ) {
        // Top Coach Controls Bar with vibrant surface & FlowRow so no chip ever crushes vertically
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        EnglishTextLtr(
                            text = activeScenario?.title ?: "AI Speaking Coach",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2
                        )
                        if (activeScenario != null) {
                            EnglishTextLtr(
                                text = "Role: ${activeScenario!!.aiRole}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1
                            )
                        }
                    }
                    IconButton(onClick = { showHistorySheet = true }) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = "Session History",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OnlineOfflineStatusBadge(isOnline, strings.onlineLabel, strings.offlineLabel)
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = correctionMode == "FLUENCY",
                            onClick = { speakingViewModel.setCorrectionMode("FLUENCY") },
                            label = { Text(strings.fluencyMode, maxLines = 1, softWrap = false) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = correctionMode == "ACCURACY",
                            onClick = { speakingViewModel.setCorrectionMode("ACCURACY") },
                            label = { Text(strings.accuracyMode, maxLines = 1, softWrap = false) }
                        )
                    }
                    items(listOf("B1/B2", "B2", "B2+/C1")) { lvl ->
                        FilterChip(
                            selected = difficulty == lvl,
                            onClick = { speakingViewModel.setDifficulty(lvl) },
                            label = { Text(lvl, maxLines = 1, softWrap = false) }
                        )
                    }
                    items(listOf(0.8f to "0.8x", 1.0f to "1.0x", 1.15f to "1.15x")) { (rate, lbl) ->
                        FilterChip(
                            selected = speechRate == rate,
                            onClick = { speakingViewModel.setSpeechRate(rate) },
                            label = { Text(lbl, maxLines = 1, softWrap = false) }
                        )
                    }
                    items(listOf("INTERNATIONAL" to "INT", "US" to "US", "UK" to "UK")) { (acc, lbl) ->
                        FilterChip(
                            selected = accent == acc,
                            onClick = { speakingViewModel.setAccent(acc) },
                            label = { Text(lbl, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                // Action Chips using FlowRow + softWrap=false so every button stays horizontal & readable
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = { speakingViewModel.repeatLastCoachSentence() },
                        label = {
                            Text(
                                strings.repeatSentence,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Replay,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                    )
                    AssistChip(
                        onClick = { speakingViewModel.toggleTranscript() },
                        label = {
                            Text(
                                if (showTranscript) strings.hideTranscript else strings.showTranscript,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (showTranscript) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f))
                    )
                    AssistChip(
                        onClick = { speakingViewModel.finishAndScoreSession() },
                        label = {
                            Text(
                                strings.endAndScoreSession,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Assessment,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
                        ),
                        border = BorderStroke(1.2.dp, AmberWarning.copy(alpha = 0.7f)),
                        modifier = Modifier.testTag("end_score_session_button")
                    )
                }
            }
        }

        // Helper Translation / Explanation Banner
        if (helperFeedback != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = helperFeedback!!,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { speakingViewModel.clearHelperFeedback() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close helper")
                    }
                }
            }
        }

        // Status / Error Banner
        when (val st = coachState) {
            is CoachUiState.Error -> {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    border = BorderStroke(1.dp, AmberWarning),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(st.message, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
            CoachUiState.Thinking -> LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary
            )
            else -> {}
        }

        // Live Face-to-Face Human Coach Avatar Stage
        FaceToFaceCoachAvatarStage(
            voiceGender = voiceGender,
            isSpeaking = isCoachSpeaking,
            coachState = coachState,
            roleTitle = activeScenario?.aiRole,
            strings = strings,
            expanded = isAvatarExpanded,
            onToggleExpanded = { isAvatarExpanded = !isAvatarExpanded },
            onSelectVoiceGender = onSetVoiceGender,
            onReplayLast = { speakingViewModel.repeatLastCoachSentence() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        // Conversation List & Optional Report
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(turns, key = { it.id }) { turn ->
                ConversationTurnCard(
                    turn = turn,
                    showTranscript = showTranscript,
                    strings = strings,
                    onPlay = { speakingViewModel.speakOut(turn.englishText) },
                    onTranslate = { speakingViewModel.requestPersianTranslation(turn, explainOnly = false) },
                    onExplain = { speakingViewModel.requestPersianTranslation(turn, explainOnly = true) },
                    onPlayRecording = { path -> speakingViewModel.audioRecorder.playRecording(path, speechRate) }
                )
            }

            if (latestReport != null) {
                val r = latestReport!!
                item {
                    VibrantAccentCard(
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.fillMaxWidth().testTag("session_report_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (strings.isPersian) "کارنامه ارزیابی جلسه مکالمه" else "Conversation Performance Report",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                            LtrBlock {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    EnglishTextLtr("Fluency: ${r.fluencyScore}%  •  Vocab Range: ${r.vocabRangeScore}%  •  Grammar: ${r.grammarScore}%", monospace = true, style = MaterialTheme.typography.labelMedium)
                                    EnglishTextLtr("Complexity: ${r.complexityScore}%  •  Intelligibility: ${r.pronunciationScore}%  •  Spontaneous: ${r.spontaneousScore}%", monospace = true, style = MaterialTheme.typography.labelMedium)
                                    EnglishTextLtr("Hesitation: ${r.hesitationNotes}", style = MaterialTheme.typography.bodySmall)
                                    EnglishTextLtr(r.feedbackSummary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    r.recommendations.forEach { rec ->
                                        EnglishTextLtr("• $rec", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            Button(onClick = { speakingViewModel.startFreeSession() }) {
                                Text(if (strings.isPersian) "جلسه جدید" else "Start New Session", maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Voice-First Input & Prominent Microphone Area
        Surface(
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(
                        onClick = {
                            if (coachState is CoachUiState.Recording) {
                                speakingViewModel.stopVoiceTurnManually()
                            } else {
                                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                if (granted) speakingViewModel.startVoiceTurn()
                                else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.size(58.dp).testTag("coach_mic_button"),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (coachState is CoachUiState.Recording)
                                CoralAccent
                            else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (coachState is CoachUiState.Recording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = "Speak with microphone",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    OutlinedTextField(
                        value = draftInput,
                        onValueChange = { speakingViewModel.updateDraftInput(it) },
                        modifier = Modifier.weight(1f).testTag("coach_text_input"),
                        placeholder = {
                            Text(
                                if (coachState is CoachUiState.Recording)
                                    (if (strings.isPersian) "در حال گوش دادن (با مکث قطع نمی‌شود؛ پایان: توقف/ارسال)..." else "Listening (pauses OK; tap Stop or Send)...")
                                else
                                    (if (strings.isPersian) "پاسخ صوتی یا متنی (انگلیسی)..." else "Speak or type manual fallback..."),
                                maxLines = 2
                            )
                        },
                        maxLines = 3
                    )

                    FilledTonalIconButton(
                        onClick = { speakingViewModel.stopAndSendVoiceTurn() },
                        enabled = draftInput.isNotBlank(),
                        modifier = Modifier.size(48.dp).testTag("coach_send_button")
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send response")
                    }
                }
            }
        }
    }

    if (showHistorySheet) {
        AlertDialog(
            onDismissRequest = { showHistorySheet = false },
            title = { Text(if (strings.isPersian) "تاریخچه مکالمات" else "Conversation History") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pastSessions) { session ->
                        Surface(
                            onClick = {
                                speakingViewModel.loadExistingSession(session)
                                showHistorySheet = false
                            },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                EnglishTextLtr(session.title, fontWeight = FontWeight.Bold)
                                EnglishTextLtr(
                                    "${session.mode} • ${if (session.isOnlineAi) "Online AI" else "Offline"} • Fluency ${session.fluencyScore}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    monospace = true
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistorySheet = false }) {
                    Text(if (strings.isPersian) "بستن" else "Close", maxLines = 1, softWrap = false)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConversationTurnCard(
    turn: ConversationTurnEntity,
    showTranscript: Boolean,
    strings: UiStrings,
    onPlay: () -> Unit,
    onTranslate: () -> Unit,
    onExplain: () -> Unit,
    onPlayRecording: (String) -> Unit
) {
    val isUser = turn.speaker == "USER"
    val accentColor = if (isUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val correction = remember(turn.correctionJson) {
        if (turn.correctionJson.isBlank()) null
        else runCatching { coachJson.decodeFromString<TurnCorrection>(turn.correctionJson) }.getOrNull()
    }

    VibrantAccentCard(
        accentColor = accentColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (isUser) (if (strings.isPersian) "شما" else "You") else (if (strings.isPersian) "مربی انگلیسی" else "English Pro Coach"),
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Row {
                    IconButton(onClick = onPlay) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = "Listen", tint = accentColor)
                    }
                    if (turn.audioFilePath != null) {
                        IconButton(onClick = { onPlayRecording(turn.audioFilePath) }) {
                            Icon(Icons.Filled.GraphicEq, contentDescription = "Play your recording", tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }

            if (showTranscript || isUser) {
                EnglishTextLtr(turn.englishText, style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    text = if (strings.isPersian) "متن پنهان است (برای تمرین شنیداری روی آیکون پخش بزنید)" else "Transcript hidden (tap speaker icon to listen)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isUser) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(onClick = onTranslate) {
                        Text(strings.translateToPersian, maxLines = 1, softWrap = false)
                    }
                    TextButton(onClick = onExplain) {
                        Text(strings.explainExpression, maxLines = 1, softWrap = false)
                    }
                }
            }

            if (correction != null && (correction.category != "NATURAL" || correction.explanation.isNotBlank())) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LtrBlock {
                        Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            EnglishTextLtr(
                                "Feedback [${correction.category}]",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                monospace = true
                            )
                            if (correction.correctedSentence.isNotBlank()) {
                                EnglishTextLtr("Corrected: ${correction.correctedSentence}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (correction.naturalAlternative.isNotBlank()) {
                                EnglishTextLtr("More natural: ${correction.naturalAlternative}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            if (correction.explanation.isNotBlank()) {
                                EnglishTextLtr(correction.explanation, style = MaterialTheme.typography.bodySmall)
                            }
                            if (correction.persianNote.isNotBlank()) {
                                Text(correction.persianNote, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FaceToFaceCoachAvatarStage(
    voiceGender: String,
    isSpeaking: Boolean,
    coachState: CoachUiState,
    roleTitle: String?,
    strings: UiStrings,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectVoiceGender: (String) -> Unit,
    onReplayLast: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMale = voiceGender.equals("MALE", ignoreCase = true)
    val isRecording = coachState is CoachUiState.Recording
    val isThinking = coachState is CoachUiState.Thinking

    val portraitRes = if (isMale) R.drawable.img_coach_male else R.drawable.img_coach_female
    val portraitDesc = stringResource(
        if (isMale) R.string.cd_coach_avatar_male else R.string.cd_coach_avatar_female
    )
    val partnerName = if (isMale) "David Mitchell" else "Sarah Miller"
    val partnerRole = roleTitle ?: if (isMale) {
        if (strings.isPersian) "هم‌صحبت ارشد مهندسی و مکالمه" else "Senior Engineering & Speaking Partner"
    } else {
        if (strings.isPersian) "مربی ارشد مکالمه بین‌المللی" else "Senior International Communication Coach"
    }

    val statusText = when {
        isSpeaking -> if (strings.isPersian) "در حال صحبت با شما..." else "Speaking to you..."
        isRecording -> if (strings.isPersian) "گوش می‌دهم، راحت صحبت کنید..." else "Listening attentively..."
        isThinking -> if (strings.isPersian) "در حال آماده کردن پاسخ..." else "Preparing response..."
        else -> if (strings.isPersian) "آماده مکالمه رودررو" else "Eye-to-eye • Ready"
    }

    val stateColor by animateColorAsState(
        targetValue = when {
            isSpeaking -> EmeraldSuccess
            isRecording -> CoralAccent
            isThinking -> VioletAccent
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(durationMillis = 320),
        label = "avatar_state_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_presence")
    val pulseFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking || isRecording) 950 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_pulse"
    )

    val portraitScale = when {
        isSpeaking -> 1f + (pulseFraction * 0.045f)
        isRecording -> 1f + (pulseFraction * 0.025f)
        else -> 1f + (pulseFraction * 0.01f)
    }

    Surface(
        modifier = modifier.testTag("coach_avatar_stage"),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 4.dp,
        shadowElevation = 3.dp,
        border = BorderStroke(
            width = if (isSpeaking || isRecording) 2.dp else 1.2.dp,
            color = stateColor.copy(alpha = if (isSpeaking || isRecording) 0.85f else 0.4f)
        ),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = if (expanded) 10.dp else 6.dp)
        ) {
            if (!expanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .scale(portraitScale)
                                .border(2.dp, stateColor, CircleShape)
                        )
                        Image(
                            painter = painterResource(portraitRes),
                            contentDescription = portraitDesc,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .testTag("coach_avatar_portrait")
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        EnglishTextLtr(
                            text = partnerName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AvatarVoiceWave(
                                active = isSpeaking || isRecording,
                                color = stateColor,
                                pulse = pulseFraction
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = stateColor,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = { onSelectVoiceGender(if (isMale) "FEMALE" else "MALE") },
                        modifier = Modifier.size(48.dp).testTag("avatar_gender_toggle")
                    ) {
                        Icon(
                            imageVector = if (isMale) Icons.Filled.Face6 else Icons.Filled.Face3,
                            contentDescription = if (strings.isPersian) "تغییر جنسیت هم‌صحبت" else "Switch partner voice",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onToggleExpanded,
                        modifier = Modifier.size(48.dp).testTag("avatar_expand_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = if (strings.isPersian) "بزرگ کردن تصویر هم‌صحبت" else "Expand partner view"
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header: Live Face-to-Face Call pill + Quick Male/Female Switch + Collapse button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = stateColor.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, stateColor.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .scale(if (isSpeaking || isRecording) 1f + pulseFraction * 0.4f else 1f)
                                        .clip(CircleShape)
                                        .background(stateColor)
                                )
                                Text(
                                    text = if (strings.isPersian) "مکالمه زنده رودررو" else "Live Face-to-Face",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = stateColor,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = !isMale,
                                onClick = { onSelectVoiceGender("FEMALE") },
                                label = {
                                    Text(
                                        text = if (strings.isPersian) "زن" else "Female",
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                modifier = Modifier.testTag("avatar_gender_female")
                            )
                            FilterChip(
                                selected = isMale,
                                onClick = { onSelectVoiceGender("MALE") },
                                label = {
                                    Text(
                                        text = if (strings.isPersian) "مرد" else "Male",
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                modifier = Modifier.testTag("avatar_gender_male")
                            )
                            IconButton(
                                onClick = onToggleExpanded,
                                modifier = Modifier.size(48.dp).testTag("avatar_collapse_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ExpandLess,
                                    contentDescription = if (strings.isPersian) "کوچک کردن قاب" else "Minimize portrait"
                                )
                            }
                        }
                    }

                    // Main Face-to-Face Presence Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(104.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Outer glowing pulse halo while speaking / listening
                            if (isSpeaking || isRecording || isThinking) {
                                Box(
                                    modifier = Modifier
                                        .size(102.dp)
                                        .scale(1f + pulseFraction * 0.08f)
                                        .border(
                                            width = 2.5.dp,
                                            color = stateColor.copy(alpha = 0.65f - pulseFraction * 0.35f),
                                            shape = RoundedCornerShape(26.dp)
                                        )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(2.dp, stateColor),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .size(94.dp)
                                    .scale(portraitScale)
                            ) {
                                Image(
                                    painter = painterResource(portraitRes),
                                    contentDescription = portraitDesc,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("coach_avatar_portrait")
                                )
                            }
                            // Active Call Corner Badge
                            Surface(
                                shape = CircleShape,
                                color = stateColor,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when {
                                            isSpeaking -> Icons.Filled.RecordVoiceOver
                                            isRecording -> Icons.Filled.Hearing
                                            isThinking -> Icons.Filled.AutoAwesome
                                            else -> Icons.Filled.Videocam
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EnglishTextLtr(
                                text = partnerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = partnerRole,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = stateColor.copy(alpha = 0.13f),
                                border = BorderStroke(1.dp, stateColor.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AvatarVoiceWave(
                                        active = isSpeaking || isRecording,
                                        color = stateColor,
                                        pulse = pulseFraction
                                    )
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = stateColor,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onReplayLast,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.VolumeUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (strings.isPersian) "شنیدن دوباره" else "Hear Again",
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarVoiceWave(
    active: Boolean,
    color: Color,
    pulse: Float
) {
    val heights = if (active) {
        listOf(
            6f + (pulse * 12f),
            16f - (pulse * 8f),
            8f + (pulse * 14f),
            14f - (pulse * 6f),
            6f + (pulse * 10f)
        )
    } else {
        listOf(6f, 8f, 10f, 8f, 6f)
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(22.dp)
    ) {
        heights.forEach { barH ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barH.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color.copy(alpha = if (active) 0.95f else 0.45f))
            )
        }
    }
}
