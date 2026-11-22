package com.fjnu.jin.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.fjnu.jin.data.Note
import com.fjnu.jin.data.NoteRepository
import com.fjnu.jin.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ROUTE_LIST = 0
private const val ROUTE_EDITOR = 1
private const val ROUTE_SETTINGS = 2

/**
 * 应用导航与共享状态：
 * 列表页（搜索/分类/排序）→ 编辑页（新建或修改）→ 设置页（偏好、备份）。
 */
@Composable
fun NoteApp(settings: SettingsRepository, darkTheme: Boolean) {
    val context = LocalContext.current
    val repo = remember { NoteRepository(context) }
    val scope = rememberCoroutineScope()

    var route by rememberSaveable { mutableIntStateOf(ROUTE_LIST) }
    var editingId by rememberSaveable { mutableLongStateOf(0L) }

    var query by rememberSaveable { mutableStateOf("") }
    // 空字符串代表「全部分类」，避免可空类型在 Bundle 保存时的问题
    var selectedCategoryName by rememberSaveable { mutableStateOf("") }

    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var totalCount by remember { mutableIntStateOf(0) }
    var version by remember { mutableIntStateOf(0) }

    val selectedCategory = selectedCategoryName.ifBlank { null }

    // 搜索词、分类、排序或数据版本变化时重新查询数据库
    LaunchedEffect(query, selectedCategoryName, settings.sortOrder, version) {
        val result = withContext(Dispatchers.IO) {
            Triple(
                repo.query(query, selectedCategory, settings.sortOrder),
                repo.categories(),
                repo.count()
            )
        }
        notes = result.first
        categories = result.second
        totalCount = result.third
        if (selectedCategoryName.isNotBlank() && selectedCategoryName !in categories) {
            selectedCategoryName = ""
        }
    }

    val saveNote: (Note) -> Unit = { note ->
        scope.launch {
            withContext(Dispatchers.IO) { repo.save(note) }
            version++
            route = ROUTE_LIST
            editingId = 0L
        }
    }

    val deleteNote: (Long) -> Unit = { id ->
        scope.launch {
            withContext(Dispatchers.IO) { repo.delete(id) }
            version++
            Toast.makeText(context, "已删除笔记", Toast.LENGTH_SHORT).show()
        }
    }

    val togglePin: (Note) -> Unit = { note ->
        scope.launch {
            withContext(Dispatchers.IO) { repo.setPinned(note.id, !note.pinned) }
            version++
            Toast.makeText(
                context,
                if (note.pinned) "已取消置顶" else "已置顶",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val exportAll: (Uri) -> Unit = { uri ->
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val json = repo.exportToJson()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    } ?: error("无法写入文件")
                    repo.count()
                }
            }
            val message = result.fold(
                onSuccess = { "已导出 $it 条笔记" },
                onFailure = { "导出失败：${it.message}" }
            )
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    val importBackup: (Uri) -> Unit = { uri ->
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val json = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        ?: error("无法读取文件")
                    repo.importFromJson(json)
                }
            }
            result.onSuccess { version++ }
            val message = result.fold(
                onSuccess = { "成功导入 $it 条笔记" },
                onFailure = { "导入失败：${it.message}" }
            )
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    val clearAll: () -> Unit = {
        scope.launch {
            withContext(Dispatchers.IO) { repo.clearAll() }
            version++
            Toast.makeText(context, "已清空全部笔记", Toast.LENGTH_SHORT).show()
        }
    }

    when (route) {
        ROUTE_EDITOR -> NoteEditorScreen(
            noteId = editingId,
            repo = repo,
            fontSize = settings.fontSize,
            darkTheme = darkTheme,
            onSave = saveNote,
            onClose = {
                route = ROUTE_LIST
                editingId = 0L
            },
            onDelete = { id ->
                deleteNote(id)
                route = ROUTE_LIST
                editingId = 0L
            }
        )

        ROUTE_SETTINGS -> SettingsScreen(
            settings = settings,
            noteCount = totalCount,
            onBack = { route = ROUTE_LIST },
            onExportAll = exportAll,
            onImportBackup = importBackup,
            onClearAll = clearAll
        )

        else -> NoteListScreen(
            notes = notes,
            categories = categories,
            query = query,
            onQueryChange = { query = it },
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategoryName = it ?: "" },
            sortOrder = settings.sortOrder,
            onSortOrderChange = settings::saveSortOrder,
            darkTheme = darkTheme,
            onNoteClick = {
                editingId = it.id
                route = ROUTE_EDITOR
            },
            onCreate = {
                editingId = 0L
                route = ROUTE_EDITOR
            },
            onOpenSettings = { route = ROUTE_SETTINGS },
            onTogglePin = togglePin,
            onDelete = { deleteNote(it.id) },
            onShare = { shareNote(context, it) }
        )
    }
}

/** 通过系统分享面板分享一条笔记 */
fun shareNote(context: Context, note: Note) {
    val text = buildString {
        appendLine(note.displayTitle())
        if (note.content.isNotBlank()) {
            appendLine()
            append(note.content)
        }
        appendLine()
        appendLine()
        append("—— 来自 NotePad 记事本")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, note.displayTitle())
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "分享笔记"))
}