package com.breez.notes.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import com.breez.notes.domain.model.WidgetConfig
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object WidgetBackgroundStore {
    private const val DIR = "widget_bg"
    private const val DECODE_MAX_PX = 1600
    private const val CROP_MAX_PX = 720

    fun persistOriginal(context: Context, appWidgetId: Int, uri: Uri): String? {
        val out = originalFile(context, appWidgetId)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(out).use { output -> input.copyTo(output) }
        } ?: return null
        croppedFile(context, appWidgetId).delete()
        return out.absolutePath
    }

    fun persist(context: Context, appWidgetId: Int, uri: Uri): String? =
        persistOriginal(context, appWidgetId, uri)

    fun loadOriginal(context: Context, appWidgetId: Int, path: String?): Bitmap? {
        val original = originalFile(context, appWidgetId)
        if (!original.exists()) {
            val legacy = croppedFile(context, appWidgetId)
            when {
                legacy.exists() -> runCatching { legacy.copyTo(original, overwrite = false) }
                !path.isNullOrBlank() -> {
                    val fromPath = File(path)
                    if (fromPath.exists()) runCatching { fromPath.copyTo(original, overwrite = false) }
                }
            }
        }
        if (original.exists()) return decodeFile(original.absolutePath, DECODE_MAX_PX)
        return loadBitmap(context, path, DECODE_MAX_PX)
    }

    fun loadForWidget(context: Context, config: WidgetConfig, widthPx: Int, heightPx: Int): Bitmap? {
        val source = loadOriginal(context, config.appWidgetId, config.backgroundUri) ?: return null
        val (outW, outH) = WidgetBitmap.cappedSize(widthPx, heightPx)
        return cropBitmap(
            source = source,
            outWidth = outW,
            outHeight = outH,
            scale = config.backgroundScale,
            offsetXFrac = config.backgroundOffsetX,
            offsetYFrac = config.backgroundOffsetY
        )
    }

    fun loadBitmap(context: Context, path: String?): Bitmap? = loadBitmap(context, path, CROP_MAX_PX)

    fun renderCropped(context: Context, config: WidgetConfig): String? {
        val source = loadOriginal(context, config.appWidgetId, config.backgroundUri) ?: return null
        val (outW, outH) = outputSize(config.cellWidth, config.cellHeight)
        val cropped = cropBitmap(
            source = source,
            outWidth = outW,
            outHeight = outH,
            scale = config.backgroundScale,
            offsetXFrac = config.backgroundOffsetX,
            offsetYFrac = config.backgroundOffsetY
        )
        val out = croppedFile(context, config.appWidgetId)
        FileOutputStream(out).use { stream ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 88, stream)
        }
        if (cropped !== source) cropped.recycle()
        return out.absolutePath
    }

    fun delete(context: Context, appWidgetId: Int) {
        runCatching { originalFile(context, appWidgetId).delete() }
        runCatching { croppedFile(context, appWidgetId).delete() }
        runCatching { fileFor(context, appWidgetId).delete() }
    }

    private fun cropBitmap(
        source: Bitmap,
        outWidth: Int,
        outHeight: Int,
        scale: Float,
        offsetXFrac: Float,
        offsetYFrac: Float
    ): Bitmap {
        val out = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val layout = WidgetImageTransform.layout(
            imageWidth = source.width.toFloat(),
            imageHeight = source.height.toFloat(),
            viewWidth = outWidth.toFloat(),
            viewHeight = outHeight.toFloat(),
            scale = scale,
            offsetXFrac = offsetXFrac,
            offsetYFrac = offsetYFrac
        )
        val canvas = Canvas(out)
        val dest = RectF(
            layout.left,
            layout.top,
            layout.left + layout.displayWidth,
            layout.top + layout.displayHeight
        )
        canvas.drawBitmap(source, null, dest, Paint(Paint.FILTER_BITMAP_FLAG))
        return out
    }

    private fun outputSize(cellWidth: Int, cellHeight: Int): Pair<Int, Int> {
        val width = WidgetCells.coerceWidth(cellWidth)
        val height = WidgetCells.coerceHeight(cellHeight)
        val longest = max(width, height).coerceAtLeast(1)
        val longPx = CROP_MAX_PX
        val cellPx = longPx / longest.toFloat()
        return (cellPx * width).roundToInt().coerceAtLeast(1) to
            (cellPx * height).roundToInt().coerceAtLeast(1)
    }

    private fun originalFile(context: Context, appWidgetId: Int): File {
        val dir = File(context.filesDir, DIR).apply { mkdirs() }
        return File(dir, "${appWidgetId}_src.jpg")
    }

    private fun croppedFile(context: Context, appWidgetId: Int): File {
        val dir = File(context.filesDir, DIR).apply { mkdirs() }
        return File(dir, "$appWidgetId.jpg")
    }

    private fun fileFor(context: Context, appWidgetId: Int): File = croppedFile(context, appWidgetId)

    private fun loadBitmap(context: Context, path: String?, maxPx: Int): Bitmap? {
        if (path.isNullOrBlank()) return null
        return runCatching {
            when {
                path.startsWith("content:", ignoreCase = true) -> {
                    decodeStream(context, Uri.parse(path), maxPx)
                }
                path.startsWith("file:", ignoreCase = true) -> {
                    decodeFile(Uri.parse(path).path ?: return null, maxPx)
                }
                else -> decodeFile(path, maxPx)
            }
        }.getOrNull()
    }

    private fun decodeFile(path: String, maxPx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxPx)
        }
        return BitmapFactory.decodeFile(path, options)
    }

    private fun decodeStream(context: Context, uri: Uri, maxPx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxPx)
        }
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }

    private fun sampleSize(width: Int, height: Int, maxPx: Int): Int {
        var sample = 1
        val longest = max(width, height).coerceAtLeast(1)
        while (longest / sample > maxPx) sample *= 2
        return sample
    }
}
