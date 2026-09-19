package com.breez.notes.data.repository

import android.content.Context
import android.net.Uri
import com.breez.notes.data.files.AttachmentStore
import com.breez.notes.data.local.dao.AttachmentDao
import com.breez.notes.data.local.dao.NoteDao
import com.breez.notes.data.mapper.toDomain
import com.breez.notes.data.mapper.toEntity
import com.breez.notes.domain.model.AttachmentType
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.model.NoteAttachment
import com.breez.notes.domain.model.Recurrence
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.ocr.ImageTextRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val attachmentDao: AttachmentDao,
    private val attachmentStore: AttachmentStore,
    private val imageTextRecognizer: ImageTextRecognizer,
    @ApplicationContext private val context: Context
) : NoteRepository {

    override fun observeAll(): Flow<List<Note>> =
        noteDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeArchived(): Flow<List<Note>> =
        noteDao.observeArchived().map { list -> list.map { it.toDomain() } }

    override fun observeByFolder(folderId: Long): Flow<List<Note>> =
        noteDao.observeByFolder(folderId).map { list -> list.map { it.toDomain() } }

    override fun observeWithoutFolder(): Flow<List<Note>> =
        noteDao.observeWithoutFolder().map { list -> list.map { it.toDomain() } }

    override fun observeSearch(query: String): Flow<List<Note>> =
        noteDao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Note? = noteDao.getById(id)?.toDomain()

    override suspend fun getAll(): List<Note> = noteDao.getAll().map { it.toDomain() }

    override suspend fun getByFolder(folderId: Long): List<Note> =
        noteDao.getByFolder(folderId).map { it.toDomain() }

    override suspend fun upsert(note: Note): Long {
        return if (note.id == 0L) {
            val sortOrder = if (note.sortOrder != 0) note.sortOrder else noteDao.minSortOrder() - 1
            noteDao.insert(note.toEntity().copy(id = 0L, sortOrder = sortOrder))
        } else {
            noteDao.update(note.toEntity())
            note.id
        }
    }

    override suspend fun delete(note: Note) {
        attachmentStore.deleteAll(note.id)
        noteDao.delete(note.toEntity())
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) {
        noteDao.setPinned(id, pinned, System.currentTimeMillis())
        if (pinned) {
            noteDao.setSortOrder(id, noteDao.minSortOrder() - 1)
        }
    }

    override suspend fun setArchived(id: Long, archived: Boolean) {
        noteDao.setArchived(id, archived, System.currentTimeMillis())
    }

    override suspend fun rename(id: Long, title: String) {
        val note = getById(id) ?: return
        noteDao.update(note.copy(title = title.trim(), updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun moveToFolder(id: Long, folderId: Long?) {
        noteDao.moveToFolder(id, folderId, System.currentTimeMillis())
    }

    override suspend fun copyToFolder(id: Long, folderId: Long?): Long {
        val source = getById(id) ?: return 0L
        val now = System.currentTimeMillis()
        val copy = source.copy(
            id = 0L,
            folderId = folderId,
            isPinned = false,
            isArchived = false,
            reminderAt = null,
            locationReminder = false,
            recurrence = Recurrence(),
            attachments = emptyList(),
            sortOrder = noteDao.minSortOrder() - 1,
            createdAt = now,
            updatedAt = now
        )
        val newId = noteDao.insert(copy.toEntity().copy(id = 0L))
        if (source.attachments.isNotEmpty()) {
            attachmentStore.copyAll(source.id, newId)
            source.attachments.forEach { attachment ->
                attachmentDao.insert(
                    attachment.copy(id = 0L, noteId = newId, createdAt = now).toEntity().copy(id = 0L)
                )
            }
        }
        return newId
    }

    override suspend fun reorder(notes: List<Note>) {
        val orders = sortOrdersAfterReorder(notes.map { it.sortOrder })
        notes.forEachIndexed { index, note ->
            noteDao.setSortOrder(note.id, orders[index])
        }
    }

    override suspend fun addAttachment(noteId: Long, uri: Uri, mimeType: String?): NoteAttachment {
        val mime = mimeType?.takeIf { it.isNotBlank() }
            ?: context.contentResolver.getType(uri)
            ?: "image/jpeg"
        val type = if (mime.startsWith("video")) AttachmentType.VIDEO else AttachmentType.PHOTO
        val file = attachmentStore.import(noteId, uri, mime)
        val ocr = if (type == AttachmentType.PHOTO) {
            runCatching { imageTextRecognizer.fromFile(file) }.getOrDefault("")
        } else {
            ""
        }
        val entity = NoteAttachment(
            noteId = noteId,
            type = type,
            fileName = file.name,
            mimeType = mime,
            ocrText = ocr
        ).toEntity().copy(id = 0L)
        val id = attachmentDao.insert(entity)
        return entity.copy(id = id).toDomain()
    }

    override suspend fun deleteAttachment(attachment: NoteAttachment) {
        attachmentStore.delete(attachment.noteId, attachment.fileName)
        attachmentDao.delete(attachment.toEntity())
    }
}
