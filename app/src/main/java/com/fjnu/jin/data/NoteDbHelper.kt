package com.fjnu.jin.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * 笔记数据库：使用 Android 原生 SQLite（对应实验要求中的数据存储知识点）。
 *
 * 数据库文件：/data/data/com.fjnu.jin/databases/notepad.db
 */
class NoteDbHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext, DB_NAME, null, DB_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL DEFAULT '',
                $COL_CONTENT TEXT NOT NULL DEFAULT '',
                $COL_CREATED_AT INTEGER NOT NULL,
                $COL_UPDATED_AT INTEGER NOT NULL,
                $COL_CATEGORY TEXT NOT NULL DEFAULT '${NoteCategories.DEFAULT}',
                $COL_COLOR INTEGER NOT NULL DEFAULT 0,
                $COL_PINNED INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_notes_updated ON $TABLE ($COL_UPDATED_AT)")

        // 首次创建数据库时写入一条使用说明，便于直接体验各项功能
        val now = System.currentTimeMillis()
        db.insert(
            TABLE,
            null,
            ContentValues().apply {
                put(COL_TITLE, "欢迎使用 NotePad 记事本")
                put(
                    COL_CONTENT,
                    """
                    这是一条示例笔记，可以点击卡片右侧的「⋮」菜单删除或在编辑页删除。

                    已实现的功能：
                    • 列表显示每条笔记的创建/修改时间戳
                    • 按标题或正文实时搜索笔记
                    • 笔记分类、分类筛选
                    • 置顶笔记与多种排序方式
                    • 笔记配色、浅色/深色主题、正文字号调节
                    • 导出/导入 JSON 备份，单条笔记分享
                    """.trimIndent()
                )
                put(COL_CREATED_AT, now)
                put(COL_UPDATED_AT, now)
                put(COL_CATEGORY, NoteCategories.DEFAULT)
                put(COL_COLOR, 1)
                put(COL_PINNED, 1)
            }
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 实验项目仅有一个版本，升级时重建表结构
        db.execSQL("DROP TABLE IF EXISTS $TABLE")
        onCreate(db)
    }

    companion object {
        const val DB_NAME = "notepad.db"
        const val DB_VERSION = 1

        const val TABLE = "notes"
        const val COL_ID = "_id"
        const val COL_TITLE = "title"
        const val COL_CONTENT = "content"
        const val COL_CREATED_AT = "created_at"
        const val COL_UPDATED_AT = "updated_at"
        const val COL_CATEGORY = "category"
        const val COL_COLOR = "color_index"
        const val COL_PINNED = "pinned"
    }
}
