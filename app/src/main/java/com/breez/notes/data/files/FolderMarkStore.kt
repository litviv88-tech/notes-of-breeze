package com.breez.notes.data.files

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderMarkStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun file(folderId: Long, fileName: String): File = File(dir(folderId), fileName)

    fun dir(folderId: Long): File = File(context.filesDir, "folder_marks/$folderId").apply { mkdirs() }

    fun import(folderId: Long, uri: Uri, mimeType: String): File {
        deleteAll(folderId)
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: if (mimeType.startsWith("video")) "mp4" else "jpg"
        val dest = File(dir(folderId), "${UUID.randomUUID()}.$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Не удалось прочитать файл")
        return dest
    }

    fun durationMs(uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            runCatching { retriever.release() }
        }
    }

    fun deleteAll(folderId: Long) {
        File(context.filesDir, "folder_marks/$folderId").deleteRecursively()
    }
}
