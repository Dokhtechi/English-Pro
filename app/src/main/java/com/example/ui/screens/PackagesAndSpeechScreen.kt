package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ContentPackageEntity
import com.example.speech.SpeechModelSpec
import com.example.speech.TtsStatus
import com.example.ui.components.CefrBadge
import com.example.ui.components.EnglishTextLtr
import com.example.ui.components.VibrantAccentCard
import com.example.ui.localization.UiStrings
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PackagesAndSpeechScreen(
    packages: List<ContentPackageEntity>,
    speechModels: List<SpeechModelSpec>,
    ttsStatus: TtsStatus,
    availableStorageBytes: Long,
    isOnline: Boolean,
    strings: UiStrings,
    onInstallPackage: (ContentPackageEntity) -> Unit,
    onPauseDownload: (String) -> Unit,
    onDeletePackage: (ContentPackageEntity) -> Unit,
    onDownloadVoskModel: (SpeechModelSpec) -> Unit,
    onRemoveVoskModel: (String) -> Unit,
    onRefreshSpeechStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val freeMb = availableStorageBytes / (1024 * 1024)

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("packages_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(strings.packagesTab, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            EnglishTextLtr("Available device storage: $freeMb MB", style = MaterialTheme.typography.labelMedium, monospace = true, color = MaterialTheme.colorScheme.secondary)
        }

        item {
            Text(
                text = if (strings.isPersian) "مدل‌های گفتار آفلاین (ASR & TTS)" else "Offline Speech & Voice Models (ASR & TTS)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(speechModels, key = { it.id }) { model ->
            val sizeMb = model.sizeBytes / (1024 * 1024)
            VibrantAccentCard(accentColor = VioletAccent, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        EnglishTextLtr(model.name, fontWeight = FontWeight.Bold, color = VioletAccent, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        CefrBadge("~${sizeMb} MB", accentColor = VioletAccent)
                    }
                    EnglishTextLtr("Version ${model.version} • ${model.languageAccent} • License: ${model.license}", style = MaterialTheme.typography.labelSmall)
                    EnglishTextLtr(model.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (model.isInstalled) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Installed & Verified", maxLines = 1, softWrap = false) },
                                leadingIcon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldSuccess) }
                            )
                            OutlinedButton(onClick = { onRemoveVoskModel(model.id) }, modifier = Modifier.heightIn(min = 48.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Remove", maxLines = 1, softWrap = false)
                            }
                        } else {
                            Button(
                                onClick = { onDownloadVoskModel(model) },
                                enabled = isOnline,
                                modifier = Modifier.heightIn(min = 48.dp).testTag("download_model_${model.id}")
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(if (isOnline) "Download Model (~${sizeMb} MB)" else "Internet Required", maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }
        }

        item {
            VibrantAccentCard(accentColor = VibrantTeal, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (strings.isPersian) "وضعیت صداهای آفلاین TTS دستگاه" else "Installed Offline Android TTS Voices", fontWeight = FontWeight.Bold, color = VibrantTeal)
                    EnglishTextLtr(
                        "US Offline Ready: ${ttsStatus.usOfflineAvailable}  |  UK Offline Ready: ${ttsStatus.ukOfflineAvailable}",
                        style = MaterialTheme.typography.labelMedium,
                        monospace = true
                    )
                    if (ttsStatus.installedEnglishOfflineVoices.isEmpty()) {
                        Text(
                            if (strings.isPersian) "هیچ صدای انگلیسی آفلاین نصب نشده است؛ در تنظیمات Text-to-Speech گوشی بسته English (US/UK) را دانلود کنید."
                            else "No offline English TTS voice detected. Download English (US/UK) voices in Android TTS Settings.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        ttsStatus.installedEnglishOfflineVoices.take(4).forEach { v ->
                            EnglishTextLtr("• $v", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    TextButton(onClick = onRefreshSpeechStatus) {
                        Text(if (strings.isPersian) "بررسی مجدد صداها" else "Re-check Installed Voices", maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        item {
            Text(
                text = if (strings.isPersian) "۱۰ بسته محتوای آفلاین قابل انتخاب" else "10 Selectable Offline Content Packages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(packages, key = { it.id }) { pkg ->
            val sizeMb = String.format("%.1f", pkg.sizeBytes / (1024.0 * 1024.0))
            val accent = if (pkg.status == "INSTALLED") EmeraldSuccess else VibrantRoyalBlue
            VibrantAccentCard(accentColor = accent, modifier = Modifier.fillMaxWidth().testTag("package_card_${pkg.id}")) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            EnglishTextLtr(pkg.title, fontWeight = FontWeight.Bold, color = accent)
                            Text(pkg.titleFa, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.width(8.dp))
                        CefrBadge("$sizeMb MB", accentColor = accent)
                    }
                    EnglishTextLtr(pkg.description, style = MaterialTheme.typography.bodySmall)
                    EnglishTextLtr("License: ${pkg.licenseName} • Source: ${pkg.attribution}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (pkg.status == "DOWNLOADING") {
                        LinearProgressIndicator(progress = { pkg.progressPercent / 100f }, modifier = Modifier.fillMaxWidth())
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        when (pkg.status) {
                            "INSTALLED" -> {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Installed (${pkg.installedItemCount} items)", maxLines = 1, softWrap = false) },
                                    leadingIcon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldSuccess) }
                                )
                                if (pkg.id != "pkg_core_vocab") {
                                    OutlinedButton(onClick = { onDeletePackage(pkg) }, modifier = Modifier.heightIn(min = 48.dp)) {
                                        Icon(Icons.Filled.Delete, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Remove", maxLines = 1, softWrap = false)
                                    }
                                }
                            }
                            "DOWNLOADING" -> {
                                OutlinedButton(onClick = { onPauseDownload(pkg.id) }, modifier = Modifier.heightIn(min = 48.dp)) {
                                    Icon(Icons.Filled.Pause, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Pause", maxLines = 1, softWrap = false)
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { onInstallPackage(pkg) },
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("install_pkg_${pkg.id}")
                                ) {
                                    Icon(Icons.Filled.Download, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (pkg.status == "PAUSED") "Resume" else "Install Package", maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
