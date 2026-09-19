package com.breez.notes.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.text.TextPaint
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object WidgetBitmap {
    fun forRemoteViews(
        source: Bitmap?,
        radiusPx: Float,
        widthPx: Int,
        heightPx: Int
    ): Bitmap? {
        if (source == null || source.width <= 0 || source.height <= 0) return null
        val width = widthPx.coerceAtLeast(1)
        val height = heightPx.coerceAtLeast(1)
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val dest = RectF(0f, 0f, width.toFloat(), height.toFloat())
        if (radiusPx > 0f) {
            canvas.drawRoundRect(dest, radiusPx, radiusPx, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }
        canvas.drawBitmap(source, null, dest, paint)
        return out
    }

    fun strikeText(
        text: String,
        color: Int,
        textSizePx: Float,
        widthPx: Int,
        progress: Float
    ): Bitmap {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.textSize = textSizePx
        }
        val width = widthPx.coerceAtLeast(1)
        val height = (textSizePx * 1.6f).roundToInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val y = textSizePx
        canvas.drawText(text, 0f, y, paint)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = min(3f, textSizePx * 0.12f)
            strokeCap = Paint.Cap.ROUND
        }
        val textWidth = paint.measureText(text).coerceAtMost(width.toFloat())
        val lineY = textSizePx * 0.62f
        canvas.drawLine(0f, lineY, textWidth * progress.coerceIn(0f, 1f), lineY, linePaint)
        return bitmap
    }

    fun parseColor(hex: String?, fallback: Int): Int {
        val value = hex?.trim().orEmpty()
        if (value.isEmpty()) return fallback
        return runCatching {
            Color.parseColor(if (value.startsWith("#")) value else "#$value")
        }.getOrDefault(fallback)
    }

    fun cappedSize(widthPx: Int, heightPx: Int, maxPx: Int = 1080): Pair<Int, Int> {
        val width = widthPx.coerceAtLeast(1)
        val height = heightPx.coerceAtLeast(1)
        val longest = max(width, height)
        if (longest <= maxPx) return width to height
        val factor = maxPx / longest.toFloat()
        return (width * factor).roundToInt().coerceAtLeast(1) to
            (height * factor).roundToInt().coerceAtLeast(1)
    }
}
