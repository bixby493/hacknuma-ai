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

// --- New Themes: Pink, Blue, Gray, White ---

val PinkColors = RuhanColors(
    background = Color(0xFF1A0A14),
    surface = Color(0xFF1F0E18),
    card = Color(0xFF2A1220),
    textPrimary = Color(0xFFFF69B4),
    textSecondary = Color(0xFFCC5599),
    accent = Color(0xFFFF1493),
    userBubble = Color(0xFF2A0A1E),
    ruhanBubble = Color(0xFF1F0020),
    ruhanText = Color(0xFFFF69B4),
    chipBg = Color(0xFF2A1220),
    inputBorder = Color(0xFF3A1A2A),
    isDark = true
)

val BlueColors = RuhanColors(
    background = Color(0xFF0A0F1A),
    surface = Color(0xFF0E1420),
    card = Color(0xFF12192A),
    textPrimary = Color(0xFF00BFFF),
    textSecondary = Color(0xFF3399CC),
    accent = Color(0xFF1E90FF),
    userBubble = Color(0xFF0A152A),
    ruhanBubble = Color(0xFF001A33),
    ruhanText = Color(0xFF00BFFF),
    chipBg = Color(0xFF12192A),
    inputBorder = Color(0xFF1A2A44),
    isDark = true
)

val GrayColors = RuhanColors(
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF222222),
    card = Color(0xFF2C2C2C),
    textPrimary = Color(0xFFCCCCCC),
    textSecondary = Color(0xFF888888),
    accent = Color(0xFF6C63FF),
    userBubble = Color(0xFF2A2A2A),
    ruhanBubble = Color(0xFF333333),
    ruhanText = Color(0xFFDDDDDD),
    chipBg = Color(0xFF2C2C2C),
    inputBorder = Color(0xFF444444),
    isDark = true
)

val WhiteColors = RuhanColors(
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    card = Color(0xFFEEEEEE),
    textPrimary = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF666666),
    accent = Color(0xFF00C853),
    userBubble = Color(0xFFE8F5E9),
    ruhanBubble = Color(0xFFFFFFFF),
    ruhanText = Color(0xFF1A1A1A),
    chipBg = Color(0xFFE0E0E0),
    inputBorder = Color(0xFFBDBDBD),
    isDark = false
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
        "dark" -> DarkColors
        "amoled" -> AmoledColors
        "pink" -> PinkColors
        "blue" -> BlueColors
        "gray" -> GrayColors
        "white" -> WhiteColors
        else -> HackerColors
    }

    val materialScheme = if (ruhanColors.isDark) {
        darkColorScheme(
            background = ruhanColors.background,
            surface = ruhanColors.surface,
            primary = ruhanColors.accent,
            onPrimary = Color.Black,
            onBackground = ruhanColors.textPrimary,
            onSurface = ruhanColors.textPrimary
        )
    } else {
        androidx.compose.material3.lightColorScheme(
            background = ruhanColors.background,
            surface = ruhanColors.surface,
            primary = ruhanColors.accent,
            onPrimary = Color.White,
            onBackground = ruhanColors.textPrimary,
            onSurface = ruhanColors.textPrimary
        )
    }

    CompositionLocalProvider(LocalRuhanColors provides ruhanColors) {
        MaterialTheme(
            colorScheme = materialScheme,
            content = content
        )
    }
}
