package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.device.DeviceDiagnosticsManager
import com.example.device.DeviceDiagnosticsReport
import com.example.device.FeatureImplementationStatus
import com.example.device.FeatureMatrixItem
import com.example.ui.components.CefrBadge
import com.example.ui.components.EnglishTextLtr
import com.example.ui.components.VibrantAccentCard
import com.example.ui.localization.UiStrings
import com.example.ui.theme.*

@Composable
fun DiagnosticsScreen(
    report: DeviceDiagnosticsReport?,
    featureMatrix: List<FeatureMatrixItem>,
    strings: UiStrings,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("diagnostics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.diagnosticsTab, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh diagnostics", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (report != null) {
            item {
                VibrantAccentCard(accentColor = VibrantRoyalBlue, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (strings.isPersian) "مشخصات سخت‌افزاری و وضعیت آفلاین" else "Hardware & Offline Capability Report", fontWeight = FontWeight.Bold, color = VibrantRoyalBlue)
                        DiagRow("Device", "${report.manufacturer} ${report.model}")
                        DiagRow("OS / Xiaomi Layer", "Android ${report.androidVersion} (API ${report.apiLevel}) • ${report.hyperOsOrMiuiHint}")
                        DiagRow("Available Storage", "${report.availableStorageBytes / (1024 * 1024)} MB / ${report.totalStorageBytes / (1024 * 1024)} MB")
                        DiagRow("Available RAM", if (report.availableRamBytes != null && report.totalRamBytes != null) "${report.availableRamBytes / (1024 * 1024)} MB / ${report.totalRamBytes / (1024 * 1024)} MB" else "Not measurable")
                        DiagRow("Network Status", "${if (report.isInternetConnected) "Online" else "Offline"} (${report.connectionType})")
                        DiagRow("Offline TTS Voices", "${report.installedOfflineTtsVoices.size} installed")
                        DiagRow("Android On-Device ASR", "${report.speechDiagnostics.androidOnDeviceAvailable}")
                        DiagRow("Speech Services", report.speechDiagnostics.availableRecognitionServices.joinToString(", ").ifBlank { "None detected" })
                        DiagRow("Vosk US / UK Installed", "${report.speechDiagnostics.voskUsModelInstalled} / ${report.speechDiagnostics.voskUkModelInstalled}")
                        EnglishTextLtr(report.offlineModelCompatibilityNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        item {
            VibrantAccentCard(accentColor = VibrantTeal, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (strings.isPersian) "راهنمای اختیاری بهینه‌سازی شیائومی (MIUI / HyperOS)" else "Optional Xiaomi MIUI / HyperOS Guidance",
                        fontWeight = FontWeight.Bold,
                        color = VibrantTeal
                    )
                    Text(
                        text = if (strings.isPersian)
                            "یادگیری روزانه به اجرای مداوم در پس‌زمینه وابسته نیست و پیشرفت شما بلافاصله در Room ذخیره می‌شود. تنها در صورت قطع دانلود بسته‌های بزرگ، در تنظیمات برنامه Battery Saver را روی No restrictions بگذارید (رفتار بین مدل‌های مختلف شیائومی یکسان نیست)."
                        else
                            "Daily learning does NOT require persistent background execution—all progress persists immediately in Room. Only if large package downloads pause on MIUI/HyperOS, set Battery Saver to 'No restrictions' in App Info (exact behavior varies by Xiaomi model).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(
                        onClick = { DeviceDiagnosticsManager.openAppBatterySettings(context) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (strings.isPersian) "باز کردن تنظیمات برنامه" else "Open App Battery Settings", maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        item {
            Text(
                text = if (strings.isPersian) "وضعیت واقعی پیاده‌سازی قابلیت‌ها" else "Transparent Feature Implementation Matrix",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(featureMatrix) { item ->
            val accent = when (item.status) {
                FeatureImplementationStatus.FULLY_IMPLEMENTED_TESTED -> EmeraldSuccess
                FeatureImplementationStatus.IMPLEMENTED_PARTIAL_TEST -> VibrantRoyalBlue
                FeatureImplementationStatus.PLANNED -> VibrantAmber
                FeatureImplementationStatus.UNSUPPORTED_ON_DEVICE -> CoralAccent
            }
            VibrantAccentCard(accentColor = accent, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (strings.isPersian) item.featureNameFa else item.featureNameEn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        CefrBadge(
                            when (item.status) {
                                FeatureImplementationStatus.FULLY_IMPLEMENTED_TESTED -> "Tested"
                                FeatureImplementationStatus.IMPLEMENTED_PARTIAL_TEST -> "Ready"
                                FeatureImplementationStatus.PLANNED -> "Planned"
                                FeatureImplementationStatus.UNSUPPORTED_ON_DEVICE -> "Unsupported"
                            },
                            accentColor = accent
                        )
                    }
                    Text(if (strings.isPersian) item.status.labelFa else item.status.labelEn, style = MaterialTheme.typography.labelSmall, color = accent)
                    EnglishTextLtr(item.technicalNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        EnglishTextLtr(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.45f))
        EnglishTextLtr(value, style = MaterialTheme.typography.labelSmall, monospace = true, modifier = Modifier.weight(0.55f))
    }
}
