package com.breez.notes.data.backup

import android.content.Context
import android.net.Uri
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.model.Recurrence
import com.breez.notes.domain.model.RepeatUnit
import com.breez.notes.domain.repository.FolderRepository
import com.breez.notes.domain.repository.NoteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesBackupStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notes: NoteRepository,
    private val folders: FolderRepository
) {
    suspend fun exportTo(uri: Uri) {
        val json = exportJson()
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.writer(Charsets.UTF_8).use { it.write(json) }
        } ?: error("cannot write backup")
    }

    suspend fun importFrom(uri: Uri): Int {
        val json = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.reader(Charsets.UTF_8).readText()
        } ?: error("cannot read backup")
        return importJson(json)
    }

    suspend fun exportJson(): String {
        val folderList = folders.getAll()
        val noteList = notes.getAll()
        val root = JSONObject()
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        val foldersJson = JSONArray()
        folderList.forEach { folder -> foldersJson.put(folder.toJson()) }
        root.put("folders", foldersJson)
        val notesJson = JSONArray()
        noteList.forEach { note -> notesJson.put(note.toJson()) }
        root.put("notes", notesJson)
        return root.toString(2)
    }

    suspend fun importJson(raw: String): Int {
        val root = JSONObject(raw)
        val folderIdMap = mutableMapOf<String, Long>()
        val foldersJson = root.optJSONArray("folders") ?: JSONArray()
        for (index in 0 until foldersJson.length()) {
            val item = foldersJson.getJSONObject(index)
            val oldId = item.opt("id")?.toString().orEmpty()
            val folder = Folder(
                name = item.optString("name").ifBlank { "Папка" },
                colorHex = item.optString("colorHex").ifBlank { Folder.DEFAULT_FOLDER_COLOR },
                sortOrder = item.optInt("sortOrder", index),
                createdAt = item.optLong("createdAt", System.currentTimeMillis())
            )
            val newId = folders.upsert(folder)
            if (oldId.isNotBlank()) folderIdMap[oldId] = newId
        }
        val notesJson = root.optJSONArray("notes") ?: JSONArray()
        var imported = 0
        for (index in 0 until notesJson.length()) {
            val item = notesJson.getJSONObject(index)
            val oldFolder = item.opt("folderId")?.toString()
            val recurrence = item.optJSONObject("recurrence")
            val note = Note(
                title = item.optString("title"),
                body = item.optString("body"),
                folderId = oldFolder?.let { folderIdMap[it] },
                colorHex = item.optString("colorHex").ifBlank { Note.DEFAULT_NOTE_COLOR },
                isPinned = item.optBoolean("isPinned") || item.optBoolean("pinned"),
                sortOrder = item.optInt("sortOrder", index),
                reminderAt = item.optLongOrNull("reminderAt"),
                meetingPlace = item.optString("meetingPlace"),
                meetingLat = item.optDoubleOrNull("meetingLat"),
                meetingLng = item.optDoubleOrNull("meetingLng"),
                locationReminder = item.optBoolean("locationReminder"),
                recurrence = Recurrence(
                    unit = runCatching {
                        RepeatUnit.valueOf(recurrence?.optString("unit") ?: RepeatUnit.NONE.name)
                    }.getOrDefault(RepeatUnit.NONE),
                    interval = recurrence?.optInt("interval", 1) ?: 1,
                    weekDays = recurrence?.optJSONArray("weekDays")?.toIntSet().orEmpty(),
                    untilAt = recurrence?.optLongOrNull("untilAt")
                ),
                isChecklist = item.optBoolean("isChecklist"),
                isArchived = item.optBoolean("isArchived"),
                createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
            )
            notes.upsert(note)
            imported++
        }
        return imported
    }

    private fun Folder.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("colorHex", colorHex)
        .put("sortOrder", sortOrder)
        .put("createdAt", createdAt)

    private fun Note.toJson(): JSONObject {
        val json = JSONObject()
            .put("id", id)
            .put("title", title)
            .put("body", body)
            .put("colorHex", colorHex)
            .put("isPinned", isPinned)
            .put("sortOrder", sortOrder)
            .put("meetingPlace", meetingPlace)
            .put("locationReminder", locationReminder)
            .put("isChecklist", isChecklist)
            .put("isArchived", isArchived)
            .put("createdAt", createdAt)
            .put("updatedAt", updatedAt)
        json.put("folderId", folderId ?: JSONObject.NULL)
        json.put("reminderAt", reminderAt ?: JSONObject.NULL)
        json.put("meetingLat", meetingLat ?: JSONObject.NULL)
        json.put("meetingLng", meetingLng ?: JSONObject.NULL)
        json.put(
            "recurrence",
            JSONObject()
                .put("unit", recurrence.unit.name)
                .put("interval", recurrence.interval)
                .put("weekDays", JSONArray(recurrence.weekDays.toList()))
                .put("untilAt", recurrence.untilAt ?: JSONObject.NULL)
        )
        return json
    }

    private fun JSONObject.optLongOrNull(key: String): Long? =
        if (has(key) && !isNull(key)) optLong(key) else null

    private fun JSONObject.optDoubleOrNull(key: String): Double? =
        if (has(key) && !isNull(key)) optDouble(key) else null

    private fun JSONArray.toIntSet(): Set<Int> =
        (0 until length()).mapNotNull { optInt(it).takeIf { value -> value != 0 || opt(it) == 0 } }.toSet()

    companion object {
        const val SCHEMA_VERSION = 3
        const val MIME_TYPE = "application/json"
        const val FILE_NAME = "breez-notes-backup.json"
    }
}
