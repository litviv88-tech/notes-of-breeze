package com.breez.notes.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.breez.notes.R
import com.breez.notes.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Отдельный виджет списка дел: скролл пунктов и вычёркивание по нажатию.
 */
class TodoWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> updateWidget(context, appWidgetManager, id) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val entry = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        ioScope.launch {
            appWidgetIds.forEach { id ->
                WidgetBackgroundStore.delete(context, id)
                entry.widgetRepository().deleteByAppWidgetId(id)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_ITEM_CLICK) return
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        WidgetClickHandler.handle(
            context = context,
            appWidgetId = appWidgetId,
            noteId = intent.getLongExtra(BreezWidgetReceiver.EXTRA_NOTE_ID, -1L),
            lineIndex = intent.getIntExtra(BreezWidgetReceiver.EXTRA_LINE_INDEX, 0),
            toggleable = intent.getBooleanExtra(BreezWidgetReceiver.EXTRA_TOGGLEABLE, false),
            done = intent.getBooleanExtra(BreezWidgetReceiver.EXTRA_DONE, false)
        )
    }

    companion object {
        const val ACTION_ITEM_CLICK = "com.breez.notes.widget.TODO_ITEM_CLICK"

        private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            ioScope.launch {
                val views = runCatching {
                    WidgetRemoteViews.build(
                        context = context,
                        appWidgetId = appWidgetId,
                        clickReceiver = TodoWidgetReceiver::class.java,
                        clickAction = ACTION_ITEM_CLICK,
                        emptyTextRes = R.string.todo_widget_empty
                    )
                }.getOrElse {
                    WidgetRemoteViews.fallback(context, R.string.todo_widget_empty)
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
            }
        }
    }
}
