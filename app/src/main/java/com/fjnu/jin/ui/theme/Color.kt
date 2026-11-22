package com.fjnu.jin.ui.theme

import androidx.compose.ui.graphics.Color

// ---------- 品牌色（墨水绿 + 暖纸色） ----------
val GreenPrimary = Color(0xFF2E6F5E)
val GreenOnPrimary = Color(0xFFFFFFFF)
val GreenContainer = Color(0xFFCDEBDD)
val GreenOnContainer = Color(0xFF0B3B2E)

val AmberSecondary = Color(0xFF7A5B2E)
val AmberContainer = Color(0xFFF7E2BE)
val BlueTertiary = Color(0xFF4A5C8A)

val PaperBackground = Color(0xFFF7F4ED)
val PaperSurface = Color(0xFFFFFDF8)
val PaperSurfaceVariant = Color(0xFFE7E2D6)
val InkOnBackground = Color(0xFF1E1F1C)
val InkOnSurfaceVariant = Color(0xFF4C4A43)
val PaperOutline = Color(0xFF7B786E)
val PaperOutlineVariant = Color(0xFFCEC8BB)

val GreenPrimaryDark = Color(0xFF8FD4BE)
val GreenOnPrimaryDark = Color(0xFF06382B)
val GreenContainerDark = Color(0xFF235244)
val GreenOnContainerDark = Color(0xFFC9F2E2)

val AmberSecondaryDark = Color(0xFFE7C68B)
val AmberContainerDark = Color(0xFF5A4621)
val BlueTertiaryDark = Color(0xFFB3C4F2)

val DarkBackground = Color(0xFF121512)
val DarkSurface = Color(0xFF191D1A)
val DarkSurfaceVariant = Color(0xFF2C322D)
val DarkOnBackground = Color(0xFFE5E3DC)
val DarkOnSurfaceVariant = Color(0xFFC4C8C1)
val DarkOutline = Color(0xFF8E948C)

// ---------- 笔记卡片配色 ----------
/** 浅色主题下 7 种笔记卡片颜色，下标 0 为默认纸白 */
val NoteColorsLight = listOf(
    Color(0xFFFFFDF8), // 默认
    Color(0xFFFFF4D6), // 奶油
    Color(0xFFE4F2EA), // 薄荷
    Color(0xFFE3EDF9), // 天空
    Color(0xFFFBE7E6), // 玫瑰
    Color(0xFFEFE8F8), // 丁香
    Color(0xFFEDEAE2)  // 石灰
)

/** 深色主题下对应的笔记卡片颜色 */
val NoteColorsDark = listOf(
    Color(0xFF1D211E),
    Color(0xFF3A3220),
    Color(0xFF1E3329),
    Color(0xFF1E2A38),
    Color(0xFF3A2426),
    Color(0xFF2E273B),
    Color(0xFF2A2A26)
)

fun noteColors(darkTheme: Boolean): List<Color> =
    if (darkTheme) NoteColorsDark else NoteColorsLight

fun noteColorOf(index: Int, darkTheme: Boolean): Color {
    val colors = noteColors(darkTheme)
    return colors[index.coerceIn(0, colors.lastIndex)]
}
