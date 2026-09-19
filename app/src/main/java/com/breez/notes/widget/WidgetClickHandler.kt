package com.breez.notes.widget

import android.content.Context
import android.content.Intent
import com.breez.notes.MainActivity
import com.breez.notes.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object WidgetClickHandler {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun handle(
        context: Context,
        appWidgetId: Int,
        noteId: Long,
        lineIndex: Int,
        toggleable: Boolean,
        done: Boolean
    ) {
        if (!toggleable) {
            context.startActivity(
                Intent(context, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            return
        }
        WidgetStrikeAnimator.start(
            context = context,
            appWidgetId = appWidgetId,
            noteId = noteId,
            lineIndex = lineIndex,
            toDone = !done
        )
    }

    fun persistToggle(context: Context, appWidgetId: Int, noteId: Long, lineIndex: Int) {
        scope.launch {
            val entry = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val repo = entry.noteRepository()
            val note = repo.getById(noteId) ?: return@launch
            repo.upsert(
                note.copy(
                    body = WidgetChecklist.toggle(note.body, note.title, lineIndex),
                    updatedAt = System.currentTimeMillis()
                )
            )
            WidgetUpdater.updateWidget(context, appWidgetId)
        }
    }
}
