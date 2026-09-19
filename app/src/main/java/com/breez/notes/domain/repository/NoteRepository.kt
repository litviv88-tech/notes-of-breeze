package com.breez.notes.domain.repository

import android.net.Uri
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.model.NoteAttachment
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeAll(): Flow<List<Note>>
    fun observeArchived(): Flow<List<Note>>
    fun observeByFolder(folderId: Long): Flow<List<Note>>
    fun observeWithoutFolder(): Flow<List<Note>>
    fun observeSearch(query: String): Flow<List<Note>>
    suspend fun getAll(): List<Note>
    suspend fun getByFolder(folderId: Long): List<Note>
    suspend fun getById(id: Long): Note?
    suspend fun upsert(note: Note): Long
    suspend fun delete(note: Note)
    suspend fun setPinned(id: Long, pinned: Boolean)
    suspend fun setArchived(id: Long, archived: Boolean)
    suspend fun rename(id: Long, title: String)
    suspend fun moveToFolder(id: Long, folderId: Long?)
    suspend fun copyToFolder(id: Long, folderId: Long?): Long
    suspend fun reorder(notes: List<Note>)
    suspend fun addAttachment(noteId: Long, uri: Uri, mimeType: String?): NoteAttachment
    suspend fun deleteAttachment(attachment: NoteAttachment)
}
