package com.breez.notes.widget

import kotlin.math.max

data class WidgetImageLayout(
    val left: Float,
    val top: Float,
    val displayWidth: Float,
    val displayHeight: Float,
    val totalScale: Float
)

object WidgetImageTransform {
    const val MIN_SCALE = 1f
    const val MAX_SCALE = 4f

    fun coerceScale(scale: Float): Float = scale.coerceIn(MIN_SCALE, MAX_SCALE)

    fun layout(
        imageWidth: Float,
        imageHeight: Float,
        viewWidth: Float,
        viewHeight: Float,
        scale: Float,
        offsetXFrac: Float,
        offsetYFrac: Float
    ): WidgetImageLayout {
        if (imageWidth <= 0f || imageHeight <= 0f || viewWidth <= 0f || viewHeight <= 0f) {
            return WidgetImageLayout(0f, 0f, viewWidth, viewHeight, 1f)
        }
        val total = coverScale(imageWidth, imageHeight, viewWidth, viewHeight) * coerceScale(scale)
        val displayWidth = imageWidth * total
        val displayHeight = imageHeight * total
        val (panX, panY) = clampedPan(
            displayWidth = displayWidth,
            displayHeight = displayHeight,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            offsetXFrac = offsetXFrac,
            offsetYFrac = offsetYFrac
        )
        return WidgetImageLayout(
            left = (viewWidth - displayWidth) / 2f + panX,
            top = (viewHeight - displayHeight) / 2f + panY,
            displayWidth = displayWidth,
            displayHeight = displayHeight,
            totalScale = total
        )
    }

    fun clampOffset(
        imageWidth: Float,
        imageHeight: Float,
        viewWidth: Float,
        viewHeight: Float,
        scale: Float,
        offsetXFrac: Float,
        offsetYFrac: Float
    ): Pair<Float, Float> {
        if (imageWidth <= 0f || imageHeight <= 0f || viewWidth <= 0f || viewHeight <= 0f) {
            return 0f to 0f
        }
        val total = coverScale(imageWidth, imageHeight, viewWidth, viewHeight) * coerceScale(scale)
        val (panX, panY) = clampedPan(
            displayWidth = imageWidth * total,
            displayHeight = imageHeight * total,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            offsetXFrac = offsetXFrac,
            offsetYFrac = offsetYFrac
        )
        return (panX / viewWidth) to (panY / viewHeight)
    }

    private fun coverScale(
        imageWidth: Float,
        imageHeight: Float,
        viewWidth: Float,
        viewHeight: Float
    ): Float = max(viewWidth / imageWidth, viewHeight / imageHeight)

    private fun clampedPan(
        displayWidth: Float,
        displayHeight: Float,
        viewWidth: Float,
        viewHeight: Float,
        offsetXFrac: Float,
        offsetYFrac: Float
    ): Pair<Float, Float> {
        val maxPanX = ((displayWidth - viewWidth) / 2f).coerceAtLeast(0f)
        val maxPanY = ((displayHeight - viewHeight) / 2f).coerceAtLeast(0f)
        val panX = (offsetXFrac * viewWidth).coerceIn(-maxPanX, maxPanX)
        val panY = (offsetYFrac * viewHeight).coerceIn(-maxPanY, maxPanY)
        return panX to panY
    }
}
