package com.ruhan.ai.assistant.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val HackerGreen = Color(0xFF00FF41)
val HackerGreenDark = Color(0xFF00CC33)
val HackerBg = Color(0xFF0A0A0A)

data class RuhanColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val userBubble: Color,
    val ruhanBubble: Color,
    val ruhanText: Color,
    val chipBg: Color,
    val inputBorder: Color,
    val isDark: Boolean
)

val HackerColors = RuhanColors(
    background = Color(0xFF0A0A0A),
    surface = Color(0xFF0D0D0D),
    card = Color(0xFF0F1A0F),
    textPrimary = HackerGreen,
    textSecondary = Color(0xFF33AA33),
    accent = HackerGreen,
    userBubble = Color(0xFF0A1F0A),
    ruhanBubble = Color(0xFF002200),
    ruhanText = HackerGreen,
    chipBg = Color(0xFF0F1A0F),
    inputBorder = Color(0xFF1A3A1A),
    isDark = true
)

val AmoledColors = RuhanColors(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    card = Color(0xFF0F1A0F),
    textPrimary = HackerGreen,
    textSecondary = Color(0xFF33AA33),
    accent = HackerGreen,
    userBubble = Color(0xFF0A1F0A),
    ruhanBubble = Color(0xFF002200),
    ruhanText = HackerGreen,
    chipBg = Color(0xFF0F1A0F),
    inputBorder = Color(0xFF1A3A1A),
    isDark = true
)

val DarkColors = RuhanColors(
    background = Color(0xFF0A0F0A),
    surface = Color(0xFF0F150F),
    card = Color(0xFF142014),
    textPrimary = HackerGreen,
    textSecondary = Color(0xFF44BB44),
    accent = HackerGreen,
    userBubble = Color(0xFF0A1F0A),
    ruhanBubble = Color(0xFF003300),
    ruhanText = HackerGreen,
    chipBg = Color(0xFF142014),
    inputBorder = Color(0xFF224422),
    isDark = true
)

val LightColors = RuhanColors(
    background = Color(0xFF0D120D),
    surface = Color(0xFF0F170F),
    card = Color(0xFF142014),
    textPrimary = HackerGreen,
    textSecondary = Color(0xFF44BB44),
    accent = HackerGreen,
    userBubble = Color(0xFF0A1F0A),
    ruhanBubble = Color(0xFF003300),
    ruhanText = HackerGreen,
    chipBg = Color(0xFF142014),
    inputBorder = Color(0xFF224422),
    isDark = true
)

val LocalRuhanColors = staticCompositionLocalOf { HackerColors }

object RuhanThemeColors {
    val current: RuhanColors
        @Composable
        get() = LocalRuhanColors.current
}

@Composable
fun RuhanTheme(
    themeMode: String = "hacker",
    content: @Composable () -> Unit
) {
    val ruhanColors = when (themeMode) {
        "light" -> LightColors
        "dark" -> DarkColors
        "amoled" -> AmoledColors
        else -> HackerColors
    }

    val materialScheme = darkColorScheme(
        background = ruhanColors.background,
        surface = ruhanColors.surface,
        primary = ruhanColors.accent,
        onPrimary = Color.Black,
        onBackground = ruhanColors.textPrimary,
        onSurface = ruhanColors.textPrimary
    )

    CompositionLocalProvider(LocalRuhanColors provides ruhanColors) {
        MaterialTheme(
            colorScheme = materialScheme,
            content = content
        )
    }
}
