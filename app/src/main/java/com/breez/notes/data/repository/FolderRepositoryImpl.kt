package com.breez.notes.data.repository

import android.net.Uri
import com.breez.notes.data.files.FolderMarkStore
import com.breez.notes.data.local.dao.FolderDao
import com.breez.notes.data.mapper.toDomain
import com.breez.notes.data.mapper.toEntity
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.FolderMarkType
import com.breez.notes.domain.repository.FolderMarkSaveResult
import com.breez.notes.domain.repository.FolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao,
    private val markStore: FolderMarkStore
) : FolderRepository {

    override fun observeAll(): Flow<List<Folder>> =
        folderDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getAll(): List<Folder> =
        folderDao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): Folder? = folderDao.getById(id)?.toDomain()

    override suspend fun upsert(folder: Folder): Long {
        return if (folder.id == 0L) {
            val sortOrder = if (folder.sortOrder != 0) folder.sortOrder else folderDao.maxSortOrder() + 1
            folderDao.insert(folder.toEntity().copy(id = 0L, sortOrder = sortOrder))
        } else {
            folderDao.update(folder.toEntity())
            folder.id
        }
    }

    override suspend fun saveWithMark(
        folder: Folder,
        mediaUri: Uri?,
        mimeType: String?
    ): FolderMarkSaveResult = withContext(Dispatchers.IO) {
        runCatching {
            if (folder.markType == FolderMarkType.VIDEO && mediaUri != null) {
                val duration = markStore.durationMs(mediaUri)
                if (duration > Folder.MAX_VIDEO_DURATION_MS + 2_000L) {
                    return@withContext FolderMarkSaveResult.VideoTooLong
                }
            }
            val resolvedType = if (
                folder.markType != FolderMarkType.COLOR &&
                mediaUri == null &&
                folder.markFileName.isBlank()
            ) {
                FolderMarkType.COLOR
            } else {
                folder.markType
            }
            val prepared = folder.copy(
                markType = resolvedType,
                markFileName = if (resolvedType == FolderMarkType.COLOR) "" else folder.markFileName
            )
            val id = upsert(prepared)
            when {
                resolvedType == FolderMarkType.COLOR -> {
                    markStore.deleteAll(id)
                    if (prepared.id == 0L || folder.markType != FolderMarkType.COLOR || folder.markFileName.isNotBlank()) {
                        folderDao.update(
                            prepared.copy(id = id, markType = FolderMarkType.COLOR, markFileName = "").toEntity()
                        )
                    }
                }
                mediaUri != null -> {
                    val mime = mimeType?.takeIf { it.isNotBlank() }
                        ?: if (resolvedType == FolderMarkType.VIDEO) "video/mp4" else "image/jpeg"
                    val dest = markStore.import(id, mediaUri, mime)
                    folderDao.update(
                        prepared.copy(id = id, markType = resolvedType, markFileName = dest.name).toEntity()
                    )
                }
            }
            FolderMarkSaveResult.Ok(id)
        }.getOrElse { FolderMarkSaveResult.Failed }
    }

    override suspend fun delete(folder: Folder) {
        markStore.deleteAll(folder.id)
        folderDao.delete(folder.toEntity())
    }

    override suspend fun reorder(folders: List<Folder>) {
        val orders = sortOrdersAfterReorder(folders.map { it.sortOrder })
        folders.forEachIndexed { index, folder ->
            folderDao.setSortOrder(folder.id, orders[index])
        }
    }
}
