package com.example.ui.screens

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileEntity
import com.example.speech.TtsStatus
import com.example.ui.components.AccentSelectorRow
import com.example.ui.components.CefrLevelGuideContent
import com.example.ui.components.EnglishTextLtr
import com.example.ui.components.VibrantAccentCard
import com.example.ui.localization.UiStrings
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.VibrantAmber
import com.example.ui.theme.VibrantRoyalBlue
import com.example.ui.theme.VibrantTeal
import com.example.ui.theme.VioletAccent
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BackupAndSettingsScreen(
    profile: UserProfileEntity,
    ttsStatus: TtsStatus,
    strings: UiStrings,
    onSetUiLanguage: (String) -> Unit,
    onSetDarkMode: (String) -> Unit,
    onSetDailyTarget: (Int) -> Unit,
    onSetAccentPreference: (String) -> Unit,
    onSetVoiceGender: (String) -> Unit,
    onTestVoice: () -> Unit,
    onSetFontScale: (Float) -> Unit,
    onExportBackup: (Uri, Boolean) -> Unit,
    onImportBackup: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    studyDaysCount: Int = 0,
    vocabCount: Int = 0,
    sessionsCount: Int = 0,
    onPrepareShareBackup: (Boolean, (Uri) -> Unit) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var includeRecordingsMeta by remember { mutableStateOf(true) }
    var showExportDestinationDialog by remember { mutableStateOf(false) }
    var showCefrGuideExpanded by remember { mutableStateOf(true) }

    val defaultBackupFileName = remember {
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        "english_pro_full_backup_$dateStr.json"
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) onExportBackup(uri, includeRecordingsMeta)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onImportBackup(uri)
    }

    val currentPercent = (profile.fontScale * 100f).roundToInt()

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("backup_settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                strings.backupTab,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Font Size Control Card
        item {
            VibrantAccentCard(
                accentColor = VioletAccent,
                modifier = Modifier.fillMaxWidth().testTag("font_size_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.FormatSize, contentDescription = null, tint = VioletAccent)
                            Text(
                                text = if (strings.isPersian) "اندازه فونت برنامه" else "App Font Size",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            color = VioletAccent.copy(alpha = 0.16f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            EnglishTextLtr(
                                text = "$currentPercent%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = VioletAccent,
                                monospace = true,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = { onSetFontScale((profile.fontScale - 0.05f).coerceIn(0.80f, 1.40f)) },
                            modifier = Modifier.size(48.dp).testTag("font_size_decrease_button")
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease font size")
                        }

                        Slider(
                            value = profile.fontScale,
                            onValueChange = { onSetFontScale(it) },
                            valueRange = 0.80f..1.40f,
                            steps = 11,
                            modifier = Modifier.weight(1f).testTag("font_size_slider")
                        )

                        FilledTonalIconButton(
                            onClick = { onSetFontScale((profile.fontScale + 0.05f).coerceIn(0.80f, 1.40f)) },
                            modifier = Modifier.size(48.dp).testTag("font_size_increase_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase font size")
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            0.85f to (if (strings.isPersian) "کوچک (۸۵٪)" else "Small (85%)"),
                            1.00f to (if (strings.isPersian) "عادی (۱۰۰٪)" else "Normal (100%)"),
                            1.15f to (if (strings.isPersian) "بزرگ (۱۱۵٪)" else "Large (115%)"),
                            1.30f to (if (strings.isPersian) "خیلی بزرگ (۱۳۰٪)" else "XL (130%)")
                        )
                        presets.forEach { (scale, label) ->
                            FilterChip(
                                selected = kotlin.math.abs(profile.fontScale - scale) < 0.03f,
                                onClick = { onSetFontScale(scale) },
                                label = { Text(label, maxLines = 1, softWrap = false) }
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (strings.isPersian)
                                    "پیش‌نمایش: متن فارسی خوانا برای یادگیری روزانه مهندسی عمران"
                                else
                                    "Preview: Readable daily engineering & speaking coaching",
                                fontSize = (14f * profile.fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            EnglishTextLtr(
                                text = "Site Inspection: Check rebar cover blocks before pouring.",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Interface Language (Persian RTL default / English LTR)
        item {
            VibrantAccentCard(accentColor = VibrantRoyalBlue, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (strings.isPersian) "زبان رابط کاربری (RTL / LTR)" else "Interface Language (RTL / LTR)", fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = profile.uiLanguage == "FA",
                            onClick = { onSetUiLanguage("FA") },
                            label = { Text("فارسی (پیش‌فرض RTL)", maxLines = 1, softWrap = false) },
                            modifier = Modifier.testTag("lang_fa_chip")
                        )
                        FilterChip(
                            selected = profile.uiLanguage == "EN",
                            onClick = { onSetUiLanguage("EN") },
                            label = { Text("English (LTR)", maxLines = 1, softWrap = false) },
                            modifier = Modifier.testTag("lang_en_chip")
                        )
                    }
                }
            }
        }

        // Theme Mode
        item {
            VibrantAccentCard(accentColor = VibrantAmber, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (strings.isPersian) "پوسته ظاهری (حالت شب و روز)" else "Theme Mode", fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (key, lbl) ->
                            FilterChip(
                                selected = profile.darkModePreference == key,
                                onClick = { onSetDarkMode(key) },
                                label = { Text(lbl, maxLines = 1, softWrap = false) }
                            )
                        }
                    }
                }
            }
        }

        // Daily Study Target (30 / 60 default / 90)
        item {
            VibrantAccentCard(accentColor = EmeraldSuccess, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (strings.isPersian) "مدت مطالعه روزانه (پیش‌فرض ۶۰ دقیقه)" else "Daily Study Plan Duration (60 min default)", fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(30, 60, 90).forEach { mins ->
                            FilterChip(
                                selected = profile.dailyMinutesTarget == mins,
                                onClick = { onSetDailyTarget(mins) },
                                label = { Text("$mins min", maxLines = 1, softWrap = false) }
                            )
                        }
                    }
                }
            }
        }

        // Accent Preference
        item {
            VibrantAccentCard(accentColor = VibrantTeal, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (strings.isPersian) "تنظیم لهجه آموزشی (آمریکایی / بریتانیایی / بین‌المللی)" else "Learning Accent Preference", fontWeight = FontWeight.Bold)
                    AccentSelectorRow(
                        selectedAccent = profile.accentPreference,
                        onSelect = onSetAccentPreference
                    )
                    Text(
                        text = if (strings.isPersian)
                            "حالت پیش‌فرض بین‌المللی تعادل بین لهجه آمریکایی و بریتانیایی برقرار می‌کند و برای تمرین‌های فنی از صدای پیش‌فرض آمریکایی استفاده می‌شود."
                        else
                            "Default International mode balances US & UK exposure, with US voice as default for technical exercises.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // TTS Voice Gender Preference (Male / Female)
        item {
            VibrantAccentCard(
                accentColor = CoralAccent,
                modifier = Modifier.fillMaxWidth().testTag("voice_gender_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = CoralAccent)
                        Text(
                            text = if (strings.isPersian) "انتخاب صدای گوینده (مرد / زن)" else "Spoken Voice Selection (Male / Female)",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = profile.voiceGender == "FEMALE",
                            onClick = { onSetVoiceGender("FEMALE") },
                            label = {
                                Text(
                                    if (strings.isPersian) "صدای زن (Female)" else "Female Voice",
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            modifier = Modifier.testTag("voice_gender_female_chip")
                        )
                        FilterChip(
                            selected = profile.voiceGender == "MALE",
                            onClick = { onSetVoiceGender("MALE") },
                            label = {
                                Text(
                                    if (strings.isPersian) "صدای مرد (Male)" else "Male Voice",
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            modifier = Modifier.testTag("voice_gender_male_chip")
                        )
                    }

                    Text(
                        text = if (strings.isPersian) {
                            if (ttsStatus.distinctMaleVoiceAvailable && ttsStatus.distinctFemaleVoiceAvailable)
                                "صداهای مجزای مرد و زن روی موتور TTS دستگاه شناسایی شد."
                            else
                                "در صورت نبود بسته صدای مجزا روی دستگاه، تنظیم صدا به صورت خودکار با تغییر بسته صوتی یا تن صدا اعمال می‌شود."
                        } else {
                            if (ttsStatus.distinctMaleVoiceAvailable && ttsStatus.distinctFemaleVoiceAvailable)
                                "Distinct Male and Female Android TTS voices detected on device."
                            else
                                "Uses matching device voice when installed, or automatic pitch adaptation when only one local voice is installed."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FilledTonalButton(
                        onClick = onTestVoice,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("test_voice_gender_button")
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (strings.isPersian) "تست پخش صدای انتخاب‌شده" else "Test Selected Voice",
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Full App Backup & Restore (Structure, Appearance, All Days & Progress)
        item {
            VibrantAccentCard(
                accentColor = VibrantRoyalBlue,
                modifier = Modifier.fillMaxWidth().testTag("full_backup_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Backup, contentDescription = null, tint = VibrantRoyalBlue)
                        Text(
                            text = if (strings.isPersian)
                                "پشتیبان‌گیری و بازیابی کامل برنامه"
                            else
                                "Full App Backup & Restore",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, VibrantRoyalBlue.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (strings.isPersian)
                                    "شامل کل ساختار، تنظیمات ظاهری (تم، فونت، زبان، صدا) و تمام پیشرفت‌های شما تا امروز:"
                                else
                                    "Includes full app appearance, settings, structure & all progress tracked to date:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (strings.isPersian)
                                    "• $studyDaysCount روز سابقه مطالعه  •  $vocabCount واژه و کارت FSRS  •  $sessionsCount جلسه مکالمه، درس‌ها و آزمون‌ها"
                                else
                                    "• $studyDaysCount study days  •  $vocabCount vocab & FSRS cards  •  $sessionsCount chats, lessons & assessments",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (strings.isPersian) "شامل فایل‌ها و فهرست ضبط‌های صوتی" else "Include voice recordings & metadata",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = includeRecordingsMeta, onCheckedChange = { includeRecordingsMeta = it })
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showExportDestinationDialog = true },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("export_backup_button")
                        ) {
                            Icon(Icons.Filled.Upload, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (strings.isPersian) "خروجی و انتخاب مقصد" else "Export & Choose Destination",
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("import_backup_button")
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (strings.isPersian) "بازیابی فایل پشتیبان" else "Restore Backup",
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // About App Card ("درباره برنامه")
        item {
            VibrantAccentCard(
                accentColor = EmeraldSuccess,
                modifier = Modifier.fillMaxWidth().testTag("about_app_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = EmeraldSuccess)
                            Text(
                                text = if (strings.isPersian) "درباره برنامه" else "About App",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f))
                        ) {
                            EnglishTextLtr(
                                text = "V.1.0",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                monospace = true,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = EmeraldSuccess.copy(alpha = 0.25f))

                    AboutInfoRow(
                        icon = Icons.Filled.Verified,
                        label = if (strings.isPersian) "نام برنامه:" else "App Name:",
                        value = "English Pro"
                    )
                    AboutInfoRow(
                        icon = Icons.Filled.NewReleases,
                        label = if (strings.isPersian) "نسخه فعلی:" else "Version:",
                        value = "V.1.0"
                    )
                    AboutInfoRow(
                        icon = Icons.Filled.CalendarToday,
                        label = if (strings.isPersian) "تاریخ تولید برنامه:" else "Release Date:",
                        value = if (strings.isPersian) "۱۴۰۵/۰۸/۲۷  (۲۰۲۶/۱۱/۱۸)" else "2026-11-18  (1405/08/27)"
                    )
                    AboutInfoRow(
                        icon = Icons.Filled.Person,
                        label = if (strings.isPersian) "تهیه‌کننده برنامه:" else "Producer:",
                        value = if (strings.isPersian) "محمد عظیمی" else "Mohammad Azimi (محمد عظیمی)"
                    )

                    Text(
                        text = if (strings.isPersian)
                            "دستیار تخصصی یادگیری مکالمه انگلیسی و مهندسی عمران (آفلاین و آنلاین)"
                        else
                            "Personal English & Civil Engineering AI Speaking Coach (Offline & Online)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // CEFR Language Proficiency Ranking Guide ("راهنمای رتبه‌بندی سطح زبان")
        item {
            VibrantAccentCard(
                accentColor = VibrantTeal,
                modifier = Modifier.fillMaxWidth().testTag("cefr_level_guide_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.School, contentDescription = null, tint = VibrantTeal)
                            Text(
                                text = if (strings.isPersian)
                                    "راهنمای رتبه‌بندی سطح زبان (A1 تا C2)"
                                else
                                    "CEFR Language Level Guide (A1–C2)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { showCefrGuideExpanded = !showCefrGuideExpanded },
                            modifier = Modifier.size(48.dp).testTag("toggle_cefr_guide_button")
                        ) {
                            Icon(
                                imageVector = if (showCefrGuideExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (strings.isPersian) "باز یا بسته کردن راهنمای سطح" else "Toggle level guide"
                            )
                        }
                    }

                    if (showCefrGuideExpanded) {
                        CefrLevelGuideContent(
                            isPersian = strings.isPersian,
                            currentLevel = profile.overallCefr,
                            targetLevel = profile.targetLevel
                        )
                    } else {
                        Text(
                            text = if (strings.isPersian)
                                "پایین‌ترین سطح: A1 (مبتدی) • متوسط: B1 • فرامتوسط: B2 • پیشرفته: C1 • بالاترین سطح: C2 (تسلط کامل). برای توضیح کامل لمس کنید."
                            else
                                "Lowest: A1 • Intermediate: B1 • Upper-Int: B2 • Advanced: C1 • Highest: C2. Tap arrow to expand.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showExportDestinationDialog) {
        AlertDialog(
            onDismissRequest = { showExportDestinationDialog = false },
            title = {
                Text(
                    text = if (strings.isPersian) "انتخاب محل ذخیره یا ارسال پشتیبان" else "Choose Backup Destination",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (strings.isPersian)
                            "فایل پشتیبان کامل (شامل ظاهر برنامه و کل اطلاعات و روزهای مطالعه) به کدام مقصد ذخیره یا ارسال شود؟"
                        else
                            "Where would you like to save or send the full backup file?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    BackupDestinationOption(
                        icon = Icons.Filled.PhoneAndroid,
                        title = if (strings.isPersian) "ذخیره روی حافظه گوشی" else "Save to Phone Storage",
                        subtitle = if (strings.isPersian) "انتخاب پوشه دلخواه (دانلودها، اسناد یا کارت حافظه)" else "Pick Downloads, Documents, or SD card",
                        onClick = {
                            showExportDestinationDialog = false
                            exportLauncher.launch(defaultBackupFileName)
                        },
                        modifier = Modifier.testTag("dest_phone_storage")
                    )

                    BackupDestinationOption(
                        icon = Icons.Filled.Share,
                        title = if (strings.isPersian) "ارسال به سایر برنامه‌ها" else "Share to Other Apps",
                        subtitle = if (strings.isPersian) "باز کردن مستقیم منوی اشتراک‌گذاری گوشی (تلگرام، بله، واتساپ، ایمیل، درایو...)" else "Open Android share sheet directly (Telegram, Bale, WhatsApp, Email, Drive...)",
                        onClick = {
                            showExportDestinationDialog = false
                            onPrepareShareBackup(includeRecordingsMeta) { uri ->
                                launchBackupShare(
                                    context = context,
                                    uri = uri,
                                    isPersian = strings.isPersian,
                                    preferredPackages = emptyList()
                                )
                            }
                        },
                        modifier = Modifier.testTag("dest_other_apps")
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDestinationDialog = false }) {
                    Text(if (strings.isPersian) "انصراف" else "Cancel", maxLines = 1, softWrap = false)
                }
            }
        )
    }
}

@Composable
private fun AboutInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EmeraldSuccess,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BackupDestinationOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
}

private fun launchBackupShare(
    context: Context,
    uri: Uri,
    isPersian: Boolean,
    preferredPackages: List<String>
) {
    val shareTitle = if (isPersian) "ارسال فایل پشتیبان کامل English Pro" else "Share English Pro Full Backup"
    val baseIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "English Pro Full Backup")
        putExtra(
            Intent.EXTRA_TEXT,
            if (isPersian) "فایل پشتیبان کامل برنامه English Pro" else "English Pro full backup file"
        )
        clipData = ClipData.newRawUri("English Pro Backup", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    for (pkg in preferredPackages) {
        val targeted = Intent(baseIntent).setPackage(pkg)
        val launched = runCatching {
            context.startActivity(targeted)
            true
        }.getOrDefault(false)
        if (launched) return
    }

    val chooser = Intent.createChooser(baseIntent, shareTitle).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(chooser) }
}
