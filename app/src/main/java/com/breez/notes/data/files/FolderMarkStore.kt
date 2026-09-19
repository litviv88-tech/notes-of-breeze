package com.breez.notes.data.files

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class FolderMarkStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun file(folderId: Long, fileName: String): File = File(dir(folderId), fileName)

    fun dir(folderId: Long): File = File(context.filesDir, "folder_marks/$folderId").apply { mkdirs() }

    fun thumbnail(folderId: Long): File = File(dir(folderId), THUMB_NAME)

    fun import(folderId: Long, uri: Uri, mimeType: String): File {
        deleteAll(folderId)
        return if (mimeType.startsWith("video")) {
            importVideo(folderId, uri, mimeType)
        } else {
            importPhoto(folderId, uri)
        }
    }

    fun durationMs(uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            setRetrieverSource(retriever, uri)
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

    private fun importPhoto(folderId: Long, uri: Uri): File {
        val dest = File(dir(folderId), "${UUID.randomUUID()}.jpg")
        val bitmap = runCatching { decodePhoto(uri, MAX_PHOTO_SIDE) }.getOrNull()
        if (bitmap != null) {
            dest.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, output)
            }
            bitmap.recycle()
            if (dest.length() > 32L) return dest
            dest.delete()
        }
        return copyTo(uri, File(dir(folderId), "${UUID.randomUUID()}.jpg"))
    }

    private fun importVideo(folderId: Long, uri: Uri, mimeType: String): File {
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: "mp4"
        val dest = copyTo(uri, File(dir(folderId), "${UUID.randomUUID()}.$extension"))
        writeVideoThumb(dest, thumbnail(folderId))
        return dest
    }

    private fun copyTo(uri: Uri, dest: File): File {
        context.openUriInput(uri).use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        return dest
    }

    private fun decodePhoto(uri: Uri, maxSide: Int): Bitmap {
        val decoded = if (Build.VERSION.SDK_INT >= 28) {
            decodeModern(uri, maxSide)
        } else {
            decodeLegacy(uri, maxSide)
        }
        return scaleDown(decoded, maxSide)
    }

    private fun decodeModern(uri: Uri, maxSide: Int): Bitmap {
        val source = if (uri.scheme == "file") {
            ImageDecoder.createSource(File(uri.path ?: error("Не удалось прочитать фото")))
        } else {
            ImageDecoder.createSource(context.contentResolver, uri)
        }
        val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val longest = max(info.size.width, info.size.height)
            if (longest > maxSide) {
                val scale = longest.toFloat() / maxSide
                decoder.setTargetSize(
                    (info.size.width / scale).toInt().coerceAtLeast(1),
                    (info.size.height / scale).toInt().coerceAtLeast(1)
                )
            }
        }
        return if (decoded.config == Bitmap.Config.HARDWARE) {
            val software = decoded.copy(Bitmap.Config.ARGB_8888, false)
            decoded.recycle()
            software
        } else {
            decoded
        }
    }

    private fun decodeLegacy(uri: Uri, maxSide: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.openUriInput(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
        val sample = sampleSize(bounds.outWidth, bounds.outHeight, maxSide)
        val bitmap = context.openUriInput(uri).use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: error("Не удалось прочитать фото")
        val orientation = context.openUriInput(uri).use { input ->
            ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }
        return applyExif(bitmap, orientation)
    }

    private fun writeVideoThumb(video: File, dest: File) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(video.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val timeUs = (duration.coerceAtLeast(400L) / 8L).coerceIn(200L, 1_200L) * 1_000L
            val frame = if (Build.VERSION.SDK_INT >= 27) {
                retriever.getScaledFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 720, 720)
            } else {
                retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            } ?: retriever.frameAtTime
            if (frame != null) {
                dest.outputStream().use { output ->
                    frame.compress(Bitmap.CompressFormat.JPEG, 88, output)
                }
                frame.recycle()
            }
        } catch (_: Exception) {
            dest.delete()
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun setRetrieverSource(retriever: MediaMetadataRetriever, uri: Uri) {
        if (uri.scheme == "file") {
            retriever.setDataSource(uri.path)
        } else {
            retriever.setDataSource(context, uri)
        }
    }

    private fun sampleSize(width: Int, height: Int, maxSide: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (max(w, h) / 2 >= maxSide) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample.coerceAtLeast(1)
    }

    private fun scaleDown(bitmap: Bitmap, maxSide: Int): Bitmap {
        val longest = max(bitmap.width, bitmap.height)
        if (longest <= maxSide) return bitmap
        val scale = maxSide.toFloat() / longest
        val scaled = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }

    private fun applyExif(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    companion object {
        const val THUMB_NAME = "thumb.jpg"
        const val MAX_PHOTO_SIDE = 1440
        const val PHOTO_QUALITY = 92
    }
}
