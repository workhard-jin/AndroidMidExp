package com.fjnu.jin.data

/**
 * 一条笔记数据。
 *
 * @param category 笔记分类，用于列表筛选与编辑界面选择
 * @param colorIndex 笔记配色下标，对应 ui.theme.NoteColors 中的颜色
 * @param pinned 是否置顶，置顶笔记始终排在列表最前面
 */
data class Note(
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val category: String = NoteCategories.DEFAULT,
    val colorIndex: Int = 0,
    val pinned: Boolean = false
)

object NoteCategories {
    const val DEFAULT = "默认"

    /** 内置分类，用户也可以直接选择这些分类 */
    val PRESETS = listOf("默认", "工作", "生活", "学习", "待办")
}

/** 笔记列表排序方式 */
enum class SortOrder(val label: String) {
    UPDATED_DESC("最近修改"),
    CREATED_DESC("创建时间"),
    TITLE_ASC("标题字母序");

    companion object {
        fun fromName(name: String?): SortOrder =
            entries.firstOrNull { it.name == name } ?: UPDATED_DESC
    }
}

/** 界面主题模式 */
enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色");

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}
