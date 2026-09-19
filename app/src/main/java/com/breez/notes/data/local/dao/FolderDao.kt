package com.breez.notes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.breez.notes.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query(
        """
        SELECT folders.id, folders.name, folders.colorHex, folders.markType, folders.markFileName,
               folders.sortOrder, folders.createdAt,
               (SELECT COUNT(*) FROM notes WHERE notes.folderId = folders.id AND notes.isArchived = 0) AS noteCount
        FROM folders
        ORDER BY folders.sortOrder ASC, folders.createdAt ASC
        """
    )
    fun observeAll(): Flow<List<FolderWithCount>>

    @Query("SELECT * FROM folders ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getAll(): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FolderEntity?

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM folders")
    suspend fun maxSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity): Long

    @Update
    suspend fun update(folder: FolderEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)

    @Query("UPDATE folders SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun setSortOrder(id: Long, sortOrder: Int)
}

data class FolderWithCount(
    val id: Long,
    val name: String,
    val colorHex: String,
    val markType: String,
    val markFileName: String,
    val sortOrder: Int,
    val createdAt: Long,
    val noteCount: Int
)
