package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.VibrantAccentCard
import com.example.ui.localization.UiStrings
import com.example.ui.theme.*

private data class ToolEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val onClick: () -> Unit
)

@Composable
fun ToolsHubScreen(
    strings: UiStrings,
    onOpenListening: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenPackages: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenAssessment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = listOf(
        ToolEntry(
            "listening",
            strings.listeningTab,
            if (strings.isPersian) "تمرین شنیداری بدون متن + سایه‌زنی (0.65x / 0.8x / 1.0x)" else "Blind listening drills + multi-speed shadowing studio",
            Icons.Filled.Headphones,
            VibrantTeal,
            onOpenListening
        ),
        ToolEntry(
            "analytics",
            strings.analyticsTab,
            if (strings.isPersian) "نمودارهای هفتگی، واژگان فعال/غیرفعال و ۳ مهارت ضعیف‌تر" else "Weekly study charts, active/passive split & weakest skills",
            Icons.Filled.BarChart,
            EmeraldSuccess,
            onOpenAnalytics
        ),
        ToolEntry(
            "packages",
            strings.packagesTab,
            if (strings.isPersian) "۱۰ بسته محتوای آفلاین + مدل گفتار Vosk (~40 MB)" else "10 selectable offline packages + Vosk speech models",
            Icons.Filled.CloudDownload,
            VibrantRoyalBlue,
            onOpenPackages
        ),
        ToolEntry(
            "assessment",
            if (strings.isPersian) "آزمون تعیین سطح و ارزیابی ۳۰ روزه" else "Placement & 30-Day Re-Assessment",
            if (strings.isPersian) "سنجش ۵ مهارت و به‌روزرسانی پروفایل CEFR" else "5-skill CEFR evaluation & historical comparison",
            Icons.Filled.Quiz,
            VibrantAmber,
            onOpenAssessment
        ),
        ToolEntry(
            "diagnostics",
            strings.diagnosticsTab,
            if (strings.isPersian) "بررسی حافظه، صداهای TTS، مدل آفلاین و راهنمای MIUI/HyperOS" else "RAM, storage, offline TTS/ASR check & feature matrix",
            Icons.Filled.PhoneAndroid,
            CoralAccent,
            onOpenDiagnostics
        ),
        ToolEntry(
            "backup",
            strings.backupTab,
            if (strings.isPersian) "تغییر اندازه فونت، زبان فارسی/انگلیسی، حالت شب و پشتیبان JSON" else "Font size, Persian/English UI, theme, study plan & JSON backup",
            Icons.Filled.SettingsBackupRestore,
            VioletAccent,
            onOpenBackup
        )
    )

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("tools_hub_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                strings.navTools,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        items(entries, key = { it.id }) { entry ->
            VibrantAccentCard(
                accentColor = entry.accentColor,
                onClick = entry.onClick,
                modifier = Modifier.fillMaxWidth().testTag("tool_entry_${entry.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = entry.accentColor.copy(alpha = 0.16f)
                    ) {
                        Icon(
                            entry.icon,
                            contentDescription = null,
                            tint = entry.accentColor,
                            modifier = Modifier.padding(10.dp).size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = entry.accentColor)
                        Text(entry.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
