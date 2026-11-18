package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = VibrantRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = VibrantBlueContainer,
    onPrimaryContainer = OnVibrantBlueContainer,
    secondary = VibrantTeal,
    onSecondary = Color.White,
    secondaryContainer = VibrantTealContainer,
    onSecondaryContainer = OnVibrantTealContainer,
    tertiary = VibrantAmber,
    onTertiary = Color.White,
    tertiaryContainer = VibrantAmberContainer,
    onTertiaryContainer = OnVibrantAmberContainer,
    background = WarmLightBg,
    onBackground = InkCharcoal,
    surface = CrispWhiteCard,
    onSurface = InkCharcoal,
    surfaceVariant = SoftTintCard,
    onSurfaceVariant = SlateMuted,
    outline = VibrantBorderLight,
    error = CrimsonError,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = LuminousSky,
    onPrimary = Color(0xFF061B36),
    primaryContainer = LuminousBlueContainer,
    onPrimaryContainer = OnLuminousBlueContainer,
    secondary = LuminousMint,
    onSecondary = Color(0xFF042622),
    secondaryContainer = LuminousTealContainer,
    onSecondaryContainer = OnLuminousTealContainer,
    tertiary = LuminousGold,
    onTertiary = Color(0xFF311B00),
    tertiaryContainer = LuminousAmberContainer,
    onTertiaryContainer = OnLuminousAmberContainer,
    background = RichMidnightBg,
    onBackground = IvoryText,
    surface = RichIndigoSurface,
    onSurface = IvoryText,
    surfaceVariant = RichElevatedSurface,
    onSurfaceVariant = CoolSilverText,
    outline = VibrantBorderDark,
    error = CrimsonErrorDark,
    onError = Color(0xFF330606)
)

val EnglishProShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun EnglishProTheme(
    darkThemePreference: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val isDark = when (darkThemePreference) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        typography = EnglishProTypography,
        shapes = EnglishProShapes,
        content = content
    )
}
