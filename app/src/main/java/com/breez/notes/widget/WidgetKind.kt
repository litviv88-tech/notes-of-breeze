package com.breez.notes.widget

import android.appwidget.AppWidgetManager
import android.content.Context

enum class WidgetKind {
    NOTES,
    TODO
}

object WidgetKinds {
    fun of(context: Context, appWidgetId: Int): WidgetKind {
        val provider = AppWidgetManager.getInstance(context)
            .getAppWidgetInfo(appWidgetId)
            ?.provider
            ?.className
            .orEmpty()
        return if (provider.endsWith("TodoWidgetReceiver")) {
            WidgetKind.TODO
        } else {
            WidgetKind.NOTES
        }
    }

    fun isTodo(context: Context, appWidgetId: Int): Boolean =
        of(context, appWidgetId) == WidgetKind.TODO
}
