package com.breez.notes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.breez.notes.data.local.entity.NoteEntity
import com.breez.notes.data.local.entity.NoteWithAttachments
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Transaction
    @Query("SELECT * FROM notes ORDER BY sortOrder ASC, isPinned DESC, updatedAt DESC")
    fun observeAll(): Flow<List<NoteWithAttachments>>

    @Transaction
    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY sortOrder ASC, isPinned DESC, updatedAt DESC")
    fun observeByFolder(folderId: Long): Flow<List<NoteWithAttachments>>

    @Transaction
    @Query("SELECT * FROM notes WHERE folderId IS NULL ORDER BY sortOrder ASC, isPinned DESC, updatedAt DESC")
    fun observeWithoutFolder(): Flow<List<NoteWithAttachments>>

    @Transaction
    @Query(
        """
        SELECT * FROM notes
        WHERE id IN (
            SELECT notes.id FROM notes
            LEFT JOIN attachments ON attachments.noteId = notes.id
            WHERE notes.title LIKE '%' || :query || '%'
               OR notes.body LIKE '%' || :query || '%'
               OR IFNULL(notes.meetingPlace, '') LIKE '%' || :query || '%'
               OR IFNULL(attachments.ocrText, '') LIKE '%' || :query || '%'
        )
        ORDER BY sortOrder ASC, isPinned DESC, updatedAt DESC
        """
    )
    fun search(query: String): Flow<List<NoteWithAttachments>>

    @Transaction
    @Query("SELECT * FROM notes ORDER BY sortOrder ASC, isPinned DESC, updatedAt DESC")
    suspend fun getAll(): List<NoteWithAttachments>

    @Transaction
    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY sortOrder ASC, isPinned DESC, updatedAt DESC")
    suspend fun getByFolder(folderId: Long): List<NoteWithAttachments>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NoteWithAttachments?

    @Query("SELECT COALESCE(MIN(sortOrder), 0) FROM notes")
    suspend fun minSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("UPDATE notes SET isPinned = :pinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean, updatedAt: Long)

    @Query("UPDATE notes SET folderId = :folderId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun moveToFolder(id: Long, folderId: Long?, updatedAt: Long)

    @Query("UPDATE notes SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun setSortOrder(id: Long, sortOrder: Int)
}
