package com.fjnu.jin.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import org.json.JSONArray
import org.json.JSONObject

/**
 * 笔记数据仓储：封装 SQLite 的增删改查、搜索、排序与备份逻辑。
 */
class NoteRepository(context: Context) {

    private val helper = NoteDbHelper(context)

    /** 按关键字（标题或正文）与分类查询笔记 */
    fun query(search: String, category: String?, sort: SortOrder): List<Note> {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        val keyword = search.trim()
        if (keyword.isNotEmpty()) {
            conditions += "(${NoteDbHelper.COL_TITLE} LIKE ? OR ${NoteDbHelper.COL_CONTENT} LIKE ?)"
            val like = "%$keyword%"
            args += like
            args += like
        }
        if (category != null) {
            conditions += "${NoteDbHelper.COL_CATEGORY} = ?"
            args += category
        }

        val where = if (conditions.isEmpty()) "" else " WHERE " + conditions.joinToString(" AND ")
        val orderBy = when (sort) {
            SortOrder.UPDATED_DESC ->
                "${NoteDbHelper.COL_PINNED} DESC, ${NoteDbHelper.COL_UPDATED_AT} DESC"
            SortOrder.CREATED_DESC ->
                "${NoteDbHelper.COL_PINNED} DESC, ${NoteDbHelper.COL_CREATED_AT} DESC"
            SortOrder.TITLE_ASC ->
                "${NoteDbHelper.COL_PINNED} DESC, ${NoteDbHelper.COL_TITLE} COLLATE NOCASE ASC"
        }
        val sql = "SELECT * FROM ${NoteDbHelper.TABLE}$where ORDER BY $orderBy"
        return helper.readableDatabase.rawQuery(sql, args.toTypedArray()).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.toNote())
            }
        }
    }

    fun get(id: Long): Note? =
        helper.readableDatabase.rawQuery(
            "SELECT * FROM ${NoteDbHelper.TABLE} WHERE ${NoteDbHelper.COL_ID} = ?",
            arrayOf(id.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toNote() else null
        }

    /** 新建或更新笔记，返回笔记 id */
    fun save(note: Note): Long {
        val now = System.currentTimeMillis()
        val values = ContentValues().apply {
            put(NoteDbHelper.COL_TITLE, note.title)
            put(NoteDbHelper.COL_CONTENT, note.content)
            put(NoteDbHelper.COL_CATEGORY, note.category)
            put(NoteDbHelper.COL_COLOR, note.colorIndex)
            put(NoteDbHelper.COL_PINNED, if (note.pinned) 1 else 0)
            put(NoteDbHelper.COL_UPDATED_AT, now)
        }
        return if (note.id == 0L) {
            values.put(NoteDbHelper.COL_CREATED_AT, now)
            helper.writableDatabase.insert(NoteDbHelper.TABLE, null, values)
        } else {
            helper.writableDatabase.update(
                NoteDbHelper.TABLE,
                values,
                "${NoteDbHelper.COL_ID} = ?",
                arrayOf(note.id.toString())
            )
            note.id
        }
    }

    fun delete(id: Long) {
        helper.writableDatabase.delete(
            NoteDbHelper.TABLE,
            "${NoteDbHelper.COL_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun setPinned(id: Long, pinned: Boolean) {
        helper.writableDatabase.update(
            NoteDbHelper.TABLE,
            ContentValues().apply { put(NoteDbHelper.COL_PINNED, if (pinned) 1 else 0) },
            "${NoteDbHelper.COL_ID} = ?",
            arrayOf(id.toString())
        )
    }

    /** 已存在的分类（去重），用于列表筛选栏 */
    fun categories(): List<String> =
        helper.readableDatabase.rawQuery(
            "SELECT DISTINCT ${NoteDbHelper.COL_CATEGORY} FROM ${NoteDbHelper.TABLE} " +
                "ORDER BY ${NoteDbHelper.COL_CATEGORY}",
            null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(0))
            }
        }

    fun all(): List<Note> = query("", null, SortOrder.UPDATED_DESC)

    fun count(): Int =
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM ${NoteDbHelper.TABLE}", null
        ).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    fun clearAll() {
        helper.writableDatabase.delete(NoteDbHelper.TABLE, null, null)
    }

    /** 导出全部笔记为 JSON 文本 */
    fun exportToJson(): String {
        val array = JSONArray()
        all().forEach { note ->
            array.put(
                JSONObject().apply {
                    put("title", note.title)
                    put("content", note.content)
                    put("createdAt", note.createdAt)
                    put("updatedAt", note.updatedAt)
                    put("category", note.category)
                    put("colorIndex", note.colorIndex)
                    put("pinned", note.pinned)
                }
            )
        }
        return array.toString(2)
    }

    /**
     * 从 JSON 备份导入笔记（合并模式：标题、正文、修改时间完全相同的记录会被跳过）。
     * @return 实际导入的条数
     */
    fun importFromJson(json: String): Int {
        val array = JSONArray(json)
        val db = helper.writableDatabase
        var imported = 0
        db.beginTransaction()
        try {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val title = item.optString("title", "")
                val content = item.optString("content", "")
                val updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
                if (exists(title, content, updatedAt)) continue

                val values = ContentValues().apply {
                    put(NoteDbHelper.COL_TITLE, title)
                    put(NoteDbHelper.COL_CONTENT, content)
                    put(NoteDbHelper.COL_CREATED_AT, item.optLong("createdAt", updatedAt))
                    put(NoteDbHelper.COL_UPDATED_AT, updatedAt)
                    put(
                        NoteDbHelper.COL_CATEGORY,
                        item.optString("category", NoteCategories.DEFAULT)
                    )
                    put(NoteDbHelper.COL_COLOR, item.optInt("colorIndex", 0))
                    put(NoteDbHelper.COL_PINNED, if (item.optBoolean("pinned", false)) 1 else 0)
                }
                db.insert(NoteDbHelper.TABLE, null, values)
                imported++
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return imported
    }

    private fun exists(title: String, content: String, updatedAt: Long): Boolean =
        helper.readableDatabase.rawQuery(
            "SELECT 1 FROM ${NoteDbHelper.TABLE} WHERE ${NoteDbHelper.COL_TITLE} = ? " +
                "AND ${NoteDbHelper.COL_CONTENT} = ? AND ${NoteDbHelper.COL_UPDATED_AT} = ? LIMIT 1",
            arrayOf(title, content, updatedAt.toString())
        ).use { it.moveToFirst() }

    private fun Cursor.toNote(): Note = Note(
        id = getLong(getColumnIndexOrThrow(NoteDbHelper.COL_ID)),
        title = getString(getColumnIndexOrThrow(NoteDbHelper.COL_TITLE)),
        content = getString(getColumnIndexOrThrow(NoteDbHelper.COL_CONTENT)),
        createdAt = getLong(getColumnIndexOrThrow(NoteDbHelper.COL_CREATED_AT)),
        updatedAt = getLong(getColumnIndexOrThrow(NoteDbHelper.COL_UPDATED_AT)),
        category = getString(getColumnIndexOrThrow(NoteDbHelper.COL_CATEGORY)),
        colorIndex = getInt(getColumnIndexOrThrow(NoteDbHelper.COL_COLOR)),
        pinned = getInt(getColumnIndexOrThrow(NoteDbHelper.COL_PINNED)) == 1
    )
}
