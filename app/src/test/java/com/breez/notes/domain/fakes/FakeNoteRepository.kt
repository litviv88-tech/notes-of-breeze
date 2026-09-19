package com.breez.notes.domain.fakes

import android.net.Uri
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.model.NoteAttachment
import com.breez.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNoteRepository : NoteRepository {
    private val notes = mutableListOf<Note>()
    private val flow = MutableStateFlow<List<Note>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<Note>> = flow
    override fun observeArchived(): Flow<List<Note>> = flow
    override fun observeByFolder(folderId: Long): Flow<List<Note>> = flow
    override fun observeWithoutFolder(): Flow<List<Note>> = flow
    override fun observeSearch(query: String): Flow<List<Note>> = flow

    override suspend fun getAll(): List<Note> = notes.toList()
    override suspend fun getByFolder(folderId: Long): List<Note> =
        notes.filter { it.folderId == folderId }

    override suspend fun getById(id: Long): Note? = notes.firstOrNull { it.id == id }

    override suspend fun upsert(note: Note): Long {
        val id = if (note.id == 0L) nextId++ else note.id
        val saved = note.copy(id = id)
        notes.removeAll { it.id == id }
        notes += saved
        flow.value = notes.toList()
        return id
    }

    override suspend fun delete(note: Note) {
        notes.removeAll { it.id == note.id }
        flow.value = notes.toList()
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) = Unit
    override suspend fun setArchived(id: Long, archived: Boolean) {
        val current = notes.firstOrNull { it.id == id } ?: return
        notes.removeAll { it.id == id }
        notes += current.copy(isArchived = archived, isPinned = if (archived) false else current.isPinned)
        flow.value = notes.toList()
    }

    override suspend fun rename(id: Long, title: String) {
        val note = notes.firstOrNull { it.id == id } ?: return
        upsert(note.copy(title = title.trim(), updatedAt = System.currentTimeMillis()))
    }

    override suspend fun moveToFolder(id: Long, folderId: Long?) {
        val note = notes.firstOrNull { it.id == id } ?: return
        upsert(note.copy(folderId = folderId, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun copyToFolder(id: Long, folderId: Long?): Long {
        val note = notes.firstOrNull { it.id == id } ?: return 0L
        return upsert(
            note.copy(
                id = 0L,
                folderId = folderId,
                isPinned = false,
                isArchived = false,
                reminderAt = null,
                locationReminder = false,
                attachments = emptyList()
            )
        )
    }

    override suspend fun reorder(notes: List<Note>) = Unit
    override suspend fun addAttachment(noteId: Long, uri: Uri, mimeType: String?): NoteAttachment {
        error("attachments are not used in unit tests")
    }
    override suspend fun deleteAttachment(attachment: NoteAttachment) = Unit
}
