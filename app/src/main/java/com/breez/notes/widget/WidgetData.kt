package com.breez.notes.widget

import com.breez.notes.di.WidgetEntryPoint
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.model.WidgetConfig
import com.breez.notes.domain.model.WidgetDisplayMode
import com.breez.notes.domain.model.WidgetSourceType

object WidgetData {
    suspend fun loadNotes(entry: WidgetEntryPoint, config: WidgetConfig): List<Note> {
        val repo = entry.noteRepository()
        val all = when (config.sourceType) {
            WidgetSourceType.ALL -> repo.getAll()
            WidgetSourceType.FOLDER -> {
                val folderId = config.folderId
                if (folderId == null) emptyList() else repo.getByFolder(folderId)
            }
            WidgetSourceType.NOTE -> {
                val noteId = config.noteId
                if (noteId == null) emptyList() else listOfNotNull(repo.getById(noteId))
            }
        }
        return all.filter { !it.isArchived }.take(config.maxNotes.coerceIn(1, 5))
    }

    fun buildRows(notes: List<Note>, config: WidgetConfig): List<WidgetRow> {
        val rows = when (config.displayMode) {
            WidgetDisplayMode.TITLE -> notes.map { note ->
                WidgetRow(
                    id = note.id,
                    noteId = note.id,
                    lineIndex = 0,
                    title = note.title.ifBlank { "•" },
                    body = null,
                    done = false,
                    toggleable = false
                )
            }
            WidgetDisplayMode.FULL -> notes.map { note ->
                WidgetRow(
                    id = note.id,
                    noteId = note.id,
                    lineIndex = 0,
                    title = note.title.ifBlank { "•" },
                    body = note.body.takeIf { it.isNotBlank() },
                    done = false,
                    toggleable = false
                )
            }
            WidgetDisplayMode.CHECKLIST -> notes.flatMap { note ->
                WidgetChecklist.lines(note.body, note.title).map { line ->
                    WidgetRow(
                        id = note.id * 1000 + line.index,
                        noteId = note.id,
                        lineIndex = line.index,
                        title = line.text,
                        body = null,
                        done = line.done,
                        toggleable = true
                    )
                }
            }
        }
        return rows.take(40)
    }
}

data class WidgetRow(
    val id: Long,
    val noteId: Long,
    val lineIndex: Int,
    val title: String,
    val body: String?,
    val done: Boolean,
    val toggleable: Boolean
)
