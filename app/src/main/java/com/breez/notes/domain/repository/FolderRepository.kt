package com.breez.notes.domain.repository

import android.net.Uri
import com.breez.notes.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun observeAll(): Flow<List<Folder>>
    suspend fun getById(id: Long): Folder?
    suspend fun upsert(folder: Folder): Long
    suspend fun saveWithMark(
        folder: Folder,
        mediaUri: Uri?,
        mimeType: String?
    ): FolderMarkSaveResult
    suspend fun delete(folder: Folder)
    suspend fun reorder(folders: List<Folder>)
}

sealed interface FolderMarkSaveResult {
    data class Ok(val id: Long) : FolderMarkSaveResult
    data object VideoTooLong : FolderMarkSaveResult
    data object Failed : FolderMarkSaveResult
}
