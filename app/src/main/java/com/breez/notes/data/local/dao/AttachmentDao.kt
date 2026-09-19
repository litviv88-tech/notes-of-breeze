package com.breez.notes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.breez.notes.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE noteId = :noteId ORDER BY createdAt ASC")
    fun observeByNote(noteId: Long): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AttachmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AttachmentEntity): Long

    @Delete
    suspend fun delete(entity: AttachmentEntity)

    @Query("UPDATE attachments SET ocrText = :ocrText WHERE id = :id")
    suspend fun updateOcr(id: Long, ocrText: String)
}
