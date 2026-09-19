package com.breez.notes.bridge

import android.content.Context
import android.webkit.JavascriptInterface
import com.breez.notes.di.WidgetEntryPoint
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.FolderMarkType
import com.breez.notes.domain.model.Note
import com.breez.notes.widget.WidgetUpdater
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class BreezJsBridge(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @JavascriptInterface
    fun onNotesChanged(json: String) {
        scope.launch {
            runCatching { importSnapshot(json) }
            WidgetUpdater.updateAll(appContext)
        }
    }

    private suspend fun importSnapshot(json: String) {
        val root = JSONObject(json)
        val entry = EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        val folders = root.optJSONArray("folders") ?: JSONArray()
        val notes = root.optJSONArray("notes") ?: JSONArray()
        val folderMap = mutableMapOf<String, Long>()
        val keepFolderKeys = mutableSetOf<String>()
        for (i in 0 until folders.length()) {
            val item = folders.optJSONObject(i) ?: continue
            val webId = item.optString("id")
            if (webId.isBlank()) continue
            val key = folderKey(webId)
            keepFolderKeys += key
            val storedFolderId = prefs.getLong(key, 0L)
            val roomId = if (storedFolderId != 0L && entry.folderRepository().getById(storedFolderId) != null) {
                storedFolderId
            } else {
                0L
            }
            val existing = if (roomId != 0L) entry.folderRepository().getById(roomId) else null
            val saved = entry.folderRepository().upsert(
                Folder(
                    id = roomId,
                    name = item.optString("name").ifBlank { "Папка" },
                    colorHex = item.optString("colorHex").ifBlank { Folder.DEFAULT_FOLDER_COLOR },
                    markType = existing?.markType ?: FolderMarkType.COLOR,
                    markFileName = existing?.markFileName.orEmpty(),
                    sortOrder = existing?.sortOrder ?: i,
                    createdAt = item.optLong("createdAt", System.currentTimeMillis())
                )
            )
            prefs.edit().putLong(key, saved).apply()
            folderMap[webId] = saved
        }
        val keepNoteKeys = mutableSetOf<String>()
        for (i in 0 until notes.length()) {
            val item = notes.optJSONObject(i) ?: continue
            val webId = item.optString("id")
            if (webId.isBlank()) continue
            val key = noteKey(webId)
            keepNoteKeys += key
            val storedNoteId = prefs.getLong(key, 0L)
            val existingNote = if (storedNoteId != 0L) entry.noteRepository().getById(storedNoteId) else null
            val roomId = existingNote?.id ?: 0L
            val webFolder = item.optString("folderId")
            val mappedFolder = folderMap[webFolder]
                ?: prefs.getLong(folderKey(webFolder), 0L)
            val folderId = mappedFolder.takeIf { id ->
                id != 0L && entry.folderRepository().getById(id) != null
            }
            val now = System.currentTimeMillis()
            val saved = entry.noteRepository().upsert(
                Note(
                    id = roomId,
                    title = item.optString("title"),
                    body = item.optString("body"),
                    folderId = folderId,
                    colorHex = item.optString("colorHex").ifBlank { Note.DEFAULT_NOTE_COLOR },
                    isPinned = item.optBoolean("pinned") || item.optBoolean("isPinned"),
                    sortOrder = existingNote?.sortOrder ?: i,
                    createdAt = item.optLong("createdAt", now),
                    updatedAt = item.optLong("updatedAt", now)
                )
            )
            prefs.edit().putLong(key, saved).apply()
        }
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith(NOTE_PREFIX) && it !in keepNoteKeys }.forEach { stale ->
            val staleId = prefs.getLong(stale, 0L)
            if (staleId != 0L) {
                entry.noteRepository().getById(staleId)?.let { entry.noteRepository().delete(it) }
            }
            editor.remove(stale)
        }
        prefs.all.keys.filter { it.startsWith(FOLDER_PREFIX) && it !in keepFolderKeys }.forEach { stale ->
            editor.remove(stale)
        }
        editor.apply()
    }

    private fun noteKey(webId: String) = NOTE_PREFIX + webId
    private fun folderKey(webId: String) = FOLDER_PREFIX + webId

    companion object {
        private const val PREFS = "breez_js_sync"
        private const val NOTE_PREFIX = "js_note_"
        private const val FOLDER_PREFIX = "js_folder_"
        const val JS_NAME = "BreezNative"
    }
}
