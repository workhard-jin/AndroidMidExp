package com.fjnu.jin.ui

import com.fjnu.jin.data.Note
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private val monthDayFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

/** 完整时间戳：2026-11-22 20:30 */
fun formatFullTime(millis: Long): String = fullFormat.format(Date(millis))

/**
 * 列表中使用的时间戳：今天显示「今天 HH:mm」，昨天显示「昨天 HH:mm」，
 * 其余显示完整日期时间，便于直观阅读。
 */
fun formatListTime(millis: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = millis }

    val sameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    val dayDiff = now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR)

    return when {
        sameYear && dayDiff == 0 -> "今天 " + timeFormat.format(target.time)
        sameYear && dayDiff == 1 -> "昨天 " + timeFormat.format(target.time)
        sameYear -> monthDayFormat.format(target.time)
        else -> fullFormat.format(target.time)
    }
}

fun Note.displayTitle(): String = title.ifBlank { "无标题笔记" }

/** 列表预览：去掉多余空行，最多保留一段连续文本 */
fun Note.previewText(): String {
    val compact = content.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString("  ")
    return compact
}
