package com.ruhan.ai.assistant.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val CyanAccent = Color(0xFF00E5FF)

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

val AmoledColors = RuhanColors(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    card = Color(0xFF111111),
    textPrimary = Color.White,
    textSecondary = Color.Gray,
    accent = CyanAccent,
    userBubble = Color(0xFF1A1A2E),
    ruhanBubble = Color(0xFF003344),
    ruhanText = CyanAccent,
    chipBg = Color(0xFF111111),
    inputBorder = Color(0xFF333333),
    isDark = true
)

val DarkColors = RuhanColors(
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    card = Color(0xFF252525),
    textPrimary = Color.White,
    textSecondary = Color(0xFFAAAAAA),
    accent = CyanAccent,
    userBubble = Color(0xFF1A1A2E),
    ruhanBubble = Color(0xFF003344),
    ruhanText = CyanAccent,
    chipBg = Color(0xFF252525),
    inputBorder = Color(0xFF444444),
    isDark = true
)

val LightColors = RuhanColors(
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    card = Color(0xFFE8E8E8),
    textPrimary = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF666666),
    accent = Color(0xFF0097A7),
    userBubble = Color(0xFFE3F2FD),
    ruhanBubble = Color(0xFFE0F7FA),
    ruhanText = Color(0xFF006064),
    chipBg = Color(0xFFE0E0E0),
    inputBorder = Color(0xFFBBBBBB),
    isDark = false
)

val LocalRuhanColors = staticCompositionLocalOf { AmoledColors }

object RuhanThemeColors {
    val current: RuhanColors
        @Composable
        get() = LocalRuhanColors.current
}

@Composable
fun RuhanTheme(
    themeMode: String = "amoled",
    content: @Composable () -> Unit
) {
    val ruhanColors = when (themeMode) {
        "light" -> LightColors
        "dark" -> DarkColors
        "system" -> if (isSystemInDarkTheme()) DarkColors else LightColors
        else -> AmoledColors
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
        lightColorScheme(
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
