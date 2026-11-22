package com.fjnu.jin.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 偏好设置：使用 SharedPreferences 保存主题、正文字号、默认排序方式。
 */
class SettingsRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(
        ThemeMode.fromName(prefs.getString(KEY_THEME, null))
    )
        private set

    var sortOrder by mutableStateOf(
        SortOrder.fromName(prefs.getString(KEY_SORT, null))
    )
        private set

    /** 编辑界面正文字号（sp） */
    var fontSize by mutableIntStateOf(prefs.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE))
        private set

    fun saveThemeMode(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    fun saveSortOrder(order: SortOrder) {
        sortOrder = order
        prefs.edit().putString(KEY_SORT, order.name).apply()
    }

    fun saveFontSize(size: Int) {
        fontSize = size.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        prefs.edit().putInt(KEY_FONT_SIZE, fontSize).apply()
    }

    companion object {
        private const val PREFS_NAME = "notepad_settings"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_SORT = "sort_order"
        private const val KEY_FONT_SIZE = "font_size"

        const val MIN_FONT_SIZE = 12
        const val MAX_FONT_SIZE = 28
        const val DEFAULT_FONT_SIZE = 17
    }
}
