package com.fjnu.jin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenOnContainer,
    secondary = AmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = Color(0xFF2C1F05),
    tertiary = BlueTertiary,
    onTertiary = Color.White,
    background = PaperBackground,
    onBackground = InkOnBackground,
    surface = PaperSurface,
    onSurface = InkOnBackground,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = InkOnSurfaceVariant,
    outline = PaperOutline,
    outlineVariant = PaperOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = GreenOnPrimaryDark,
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = GreenOnContainerDark,
    secondary = AmberSecondaryDark,
    onSecondary = Color(0xFF3C2E0E),
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = Color(0xFFFFEBC4),
    tertiary = BlueTertiaryDark,
    onTertiary = Color(0xFF1B2A4D),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = Color(0xFF444A44)
)

/**
 * 应用主题。默认关闭动态取色，保证浅色/深色两套配色在列表与编辑器中表现一致。
 */
@Composable
fun MidExpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
