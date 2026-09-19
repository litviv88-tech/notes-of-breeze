package com.breez.notes.data.files

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun file(noteId: Long, fileName: String): File = File(dir(noteId), fileName)

    fun dir(noteId: Long): File = File(context.filesDir, "attachments/$noteId").apply { mkdirs() }

    fun import(noteId: Long, uri: Uri, mimeType: String): File {
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: if (mimeType.startsWith("video")) "mp4" else "jpg"
        val dest = File(dir(noteId), "${UUID.randomUUID()}.$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Не удалось прочитать файл")
        return dest
    }

    fun delete(noteId: Long, fileName: String) {
        file(noteId, fileName).delete()
    }

    fun deleteAll(noteId: Long) {
        dir(noteId).deleteRecursively()
    }
}
