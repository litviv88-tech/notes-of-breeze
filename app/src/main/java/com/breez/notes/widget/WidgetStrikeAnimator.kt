package com.breez.notes.widget

import android.content.Context
import android.os.Handler
import android.os.Looper

object WidgetStrikeAnimator {
    private const val FRAME_MS = 40L
    private const val STEP = 0.125f
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var frame: Frame? = null

    data class Frame(
        val appWidgetId: Int,
        val noteId: Long,
        val lineIndex: Int,
        val toDone: Boolean,
        val progress: Float
    )

    fun current(): Frame? = frame

    fun isAnimating(appWidgetId: Int, noteId: Long, lineIndex: Int): Boolean {
        val active = frame ?: return false
        return active.appWidgetId == appWidgetId &&
            active.noteId == noteId &&
            active.lineIndex == lineIndex
    }

    fun start(
        context: Context,
        appWidgetId: Int,
        noteId: Long,
        lineIndex: Int,
        toDone: Boolean
    ) {
        handler.removeCallbacksAndMessages(null)
        frame = Frame(
            appWidgetId = appWidgetId,
            noteId = noteId,
            lineIndex = lineIndex,
            toDone = toDone,
            progress = if (toDone) 0f else 1f
        )
        WidgetUpdater.refreshList(context, appWidgetId)
        handler.postDelayed({ tick(context) }, FRAME_MS)
    }

    private fun tick(context: Context) {
        val active = frame ?: return
        val next = if (active.toDone) {
            (active.progress + STEP).coerceAtMost(1f)
        } else {
            (active.progress - STEP).coerceAtLeast(0f)
        }
        val finished = if (active.toDone) next >= 1f else next <= 0f
        frame = active.copy(progress = next)
        WidgetUpdater.refreshList(context, active.appWidgetId)
        if (finished) {
            val done = active
            frame = null
            WidgetClickHandler.persistToggle(context, done.appWidgetId, done.noteId, done.lineIndex)
        } else {
            handler.postDelayed({ tick(context) }, FRAME_MS)
        }
    }
}
