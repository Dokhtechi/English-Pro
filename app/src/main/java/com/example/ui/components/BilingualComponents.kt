package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Guarantees English learning text, IPA phonetics, and engineering terms render strictly LTR
 * even when the overall application interface is Persian RTL.
 */
@Composable
fun LtrBlock(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier = modifier) {
            content()
        }
    }
}

@Composable
fun EnglishTextLtr(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = null,
    monospace: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Text(
            text = text,
            modifier = modifier,
            style = style.copy(
                fontFamily = if (monospace) JetBrainsMonoFontFamily else JakartaSansFontFamily,
                fontWeight = fontWeight ?: style.fontWeight
            ),
            color = color,
            maxLines = maxLines,
            softWrap = softWrap,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Vibrant outlined/elevated card with a cheerful colored border & subtle tint.
 */
@Composable
fun VibrantAccentCard(
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val containerColor = if (isDark) {
        accentColor.copy(alpha = 0.10f).let { tint ->
            Color(
                red = (MaterialTheme.colorScheme.surface.red * 0.88f + tint.red * 0.12f).coerceIn(0f, 1f),
                green = (MaterialTheme.colorScheme.surface.green * 0.88f + tint.green * 0.12f).coerceIn(0f, 1f),
                blue = (MaterialTheme.colorScheme.surface.blue * 0.88f + tint.blue * 0.12f).coerceIn(0f, 1f),
                alpha = 1f
            )
        }
    } else {
        Color(
            red = (MaterialTheme.colorScheme.surface.red * 0.94f + accentColor.red * 0.06f).coerceIn(0f, 1f),
            green = (MaterialTheme.colorScheme.surface.green * 0.94f + accentColor.green * 0.06f).coerceIn(0f, 1f),
            blue = (MaterialTheme.colorScheme.surface.blue * 0.94f + accentColor.blue * 0.06f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }
    val border = BorderStroke(1.5.dp, accentColor.copy(alpha = if (isDark) 0.55f else 0.40f))
    val shape = RoundedCornerShape(18.dp)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = containerColor,
            border = border,
            tonalElevation = 2.dp
        ) {
            Column(content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = containerColor,
            border = border,
            tonalElevation = 2.dp
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun OnlineOfflineStatusBadge(
    isOnline: Boolean,
    onlineText: String,
    offlineText: String,
    modifier: Modifier = Modifier
) {
    val accent = if (isOnline) EmeraldSuccess else AmberWarning
    val container = if (isOnline) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
    val content = if (isOnline) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = container,
        border = BorderStroke(1.2.dp, accent.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = if (isOnline) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                contentDescription = if (isOnline) onlineText else offlineText,
                tint = accent,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = if (isOnline) onlineText else offlineText,
                style = MaterialTheme.typography.labelSmall,
                color = content,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CefrBadge(
    level: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = accentColor.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f))
    ) {
        EnglishTextLtr(
            text = level,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            color = accentColor,
            fontWeight = FontWeight.Bold,
            monospace = true,
            maxLines = 1,
            softWrap = false
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccentSelectorRow(
    selectedAccent: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        "INTERNATIONAL" to "INT (US+UK)",
        "US" to "US",
        "UK" to "UK"
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (key, label) ->
                FilterChip(
                    selected = selectedAccent == key,
                    onClick = { onSelect(key) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                )
            }
        }
    }
}

private data class CefrLevelInfo(
    val code: String,
    val titleFa: String,
    val titleEn: String,
    val badgeTagFa: String,
    val badgeTagEn: String,
    val descriptionFa: String,
    val descriptionEn: String,
    val ieltsApprox: String,
    val accent: Color
)

private val cefrLevelGuideItems = listOf(
    CefrLevelInfo(
        code = "A1",
        titleFa = "مبتدی پایه (Beginner)",
        titleEn = "Beginner (Lowest Level)",
        badgeTagFa = "پایین‌ترین سطح",
        badgeTagEn = "Lowest",
        descriptionFa = "پایین‌ترین سطح رسمی زبان است. زبان‌آموز فقط عبارات و جملات بسیار ساده و روزمره (مثل معرفی خود، اعداد و نیازهای اولیه) را می‌فهمد و با کمک طرف مقابل ارتباط کوتاه برقرار می‌کند.",
        descriptionEn = "Lowest official level. Understands and uses very basic everyday expressions and simple phrases for immediate needs.",
        ieltsApprox = "IELTS ~2.5 – 3.0",
        accent = CoralAccent
    ),
    CefrLevelInfo(
        code = "A2",
        titleFa = "مبتدی پیشرفته (Elementary)",
        titleEn = "Elementary",
        badgeTagFa = "پایه",
        badgeTagEn = "Basic",
        descriptionFa = "درک جملات پرکاربرد در موضوعات آشنا (خرید، خانواده، کارهای روزمره). فرد می‌تواند درباره پیشینه و مسائل ساده محیط کار با جملات کوتاه صحبت کند.",
        descriptionEn = "Understands frequently used sentences in familiar areas and handles simple, direct exchanges on routine tasks.",
        ieltsApprox = "IELTS ~3.5 – 4.0",
        accent = AmberWarning
    ),
    CefrLevelInfo(
        code = "B1",
        titleFa = "متوسط (Intermediate)",
        titleEn = "Intermediate",
        badgeTagFa = "متوسط",
        badgeTagEn = "Independent",
        descriptionFa = "شروع استقلال زبانی: زبان‌آموز در موقعیت‌های روزمره، سفر و موضوعات آشنا گلیم خود را از آب بیرون می‌کشد، می‌تواند تجربه‌ها، برنامه‌ها و دلایل نظر خود را توضیح دهد، اما در بحث‌های پیچیده فنی یا سرعت بالا هنوز مکث دارد.",
        descriptionEn = "Handles most everyday and travel situations independently, describes experiences and plans, though still pauses in complex technical discussions.",
        ieltsApprox = "IELTS ~4.5 – 5.5",
        accent = VibrantAmber
    ),
    CefrLevelInfo(
        code = "B2",
        titleFa = "فرامتوسط (Upper-Intermediate)",
        titleEn = "Upper-Intermediate",
        badgeTagFa = "فرامتوسط",
        badgeTagEn = "Fluent Work",
        descriptionFa = "توانایی گفتگوی روان و طبیعی در محیط کار و مهندسی، درک جزئیات گزارش‌های فنی، دفاع از دیدگاه خود با استدلال روشن، و تعامل راحت بدون فشار زیاد برای هر دو طرف.",
        descriptionEn = "Interacts with fluency and spontaneity in technical and workplace contexts, explains viewpoints clearly, and understands detailed reports.",
        ieltsApprox = "IELTS ~5.5 – 6.5",
        accent = VibrantRoyalBlue
    ),
    CefrLevelInfo(
        code = "C1",
        titleFa = "پیشرفته (Advanced)",
        titleEn = "Advanced",
        badgeTagFa = "حرفه‌ای",
        badgeTagEn = "Proficient",
        descriptionFa = "تسلط حرفه‌ای بالا برای پروژه‌ها و شرکت‌های بین‌المللی: صحبت روان و دقیق بدون جستجوی طولانی برای کلمات، مدیریت مذاکرات قراردادی، و درک متون پیچیده فنی با ظرافت معنایی.",
        descriptionEn = "Expresses ideas fluently and spontaneously without obvious searching; excels in international engineering negotiations and complex texts.",
        ieltsApprox = "IELTS ~7.0 – 8.0",
        accent = VibrantTeal
    ),
    CefrLevelInfo(
        code = "C2",
        titleFa = "تسلط کامل (Mastery)",
        titleEn = "Mastery (Highest Level)",
        badgeTagFa = "بالاترین سطح",
        badgeTagEn = "Highest",
        descriptionFa = "بالاترین سطح استاندارد جهانی است (هم‌تراز با فرد تحصیل‌کرده بومی). درک کامل ظرایف زبانی، اصطلاحات تخصصی و حقوقی، و بیان کاملاً دقیق، روان و طبیعی در هر شرایطی.",
        descriptionEn = "Highest CEFR proficiency level. Understands virtually everything heard or read with complete precision, nuance, and near-native fluency.",
        ieltsApprox = "IELTS ~8.5 – 9.0",
        accent = EmeraldSuccess
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CefrLevelGuideContent(
    isPersian: Boolean,
    currentLevel: String,
    targetLevel: String,
    modifier: Modifier = Modifier
) {
    val currentBase = currentLevel.removeSuffix("+")
    val targetBase = targetLevel.removeSuffix("+")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isPersian)
                "رتبه‌بندی بین‌المللی سطح زبان (CEFR) از پایین‌ترین سطح (A1) تا بالاترین سطح (C2) دسته‌بندی می‌شود. علامت (+) مثل B1+ یا B2+ به معنای عبور از نیمه بالایی آن سطح و نزدیکی به سطح بعدی است."
            else
                "The international CEFR scale ranks proficiency from the lowest level (A1) to the highest level (C2). A plus sign (+) such as B1+ or B2+ indicates upper-band readiness toward the next level.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        cefrLevelGuideItems.forEach { item ->
            val isCurrent = item.code == currentBase
            val isTarget = item.code == targetBase
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isCurrent) item.accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    width = if (isCurrent || isTarget) 1.6.dp else 1.dp,
                    color = item.accent.copy(alpha = if (isCurrent || isTarget) 0.75f else 0.30f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CefrBadge(level = item.code, accentColor = item.accent)
                        Text(
                            text = if (isPersian) item.titleFa else item.titleEn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = item.accent.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = if (isPersian) item.badgeTagFa else item.badgeTagEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = item.accent,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            EnglishTextLtr(
                                text = item.ieltsApprox,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                monospace = true,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        if (isCurrent) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = item.accent
                            ) {
                                Text(
                                    text = if (isPersian) "سطح فعلی شما ($currentLevel)" else "Current ($currentLevel)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (isTarget) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = EmeraldSuccess
                            ) {
                                Text(
                                    text = if (isPersian) "هدف شما ($targetLevel)" else "Target ($targetLevel)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isPersian) item.descriptionFa else item.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun CefrLevelGuideDialog(
    isPersian: Boolean,
    currentLevel: String,
    targetLevel: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isPersian) "راهنمای رتبه‌بندی سطح زبان (CEFR)" else "CEFR Language Level Guide",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    CefrLevelGuideContent(
                        isPersian = isPersian,
                        currentLevel = currentLevel,
                        targetLevel = targetLevel
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isPersian) "متوجه شدم" else "Got it", maxLines = 1, softWrap = false)
            }
        }
    )
}

