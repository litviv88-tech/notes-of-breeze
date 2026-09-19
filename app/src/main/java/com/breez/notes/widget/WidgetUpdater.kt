package com.breez.notes.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetUpdater {
    fun updateAll(context: Context) {
        val app = context.applicationContext
        val manager = AppWidgetManager.getInstance(app)
        val ids = manager.getAppWidgetIds(ComponentName(app, BreezWidgetReceiver::class.java))
        ids.forEach { id -> BreezWidgetReceiver.updateWidget(app, manager, id) }
    }

    fun updateWidget(context: Context, appWidgetId: Int) {
        val app = context.applicationContext
        val manager = AppWidgetManager.getInstance(app)
        BreezWidgetReceiver.updateWidget(app, manager, appWidgetId)
    }

    fun refreshList(context: Context, appWidgetId: Int) {
        AppWidgetManager.getInstance(context.applicationContext)
            .notifyAppWidgetViewDataChanged(appWidgetId, com.breez.notes.R.id.widget_list)
    }
}
