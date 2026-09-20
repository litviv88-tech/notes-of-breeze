package com.breez.notes.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import com.breez.notes.MainActivity
import com.breez.notes.R
import com.breez.notes.di.WidgetEntryPoint
import com.breez.notes.domain.model.WidgetConfig
import dagger.hilt.android.EntryPointAccessors

internal object WidgetRemoteViews {
    suspend fun build(
        context: Context,
        appWidgetId: Int,
        clickReceiver: Class<*>,
        clickAction: String,
        emptyTextRes: Int
    ): RemoteViews {
        val entry = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val kind = WidgetKinds.of(context, appWidgetId)
        val config = entry.widgetRepository().getByAppWidgetId(appWidgetId)
            ?: defaultWidgetConfig(appWidgetId, kind)
        val views = RemoteViews(context.packageName, R.layout.breez_widget)
        val radiusPx = config.cornerRadiusDp.coerceIn(0, 32) * context.resources.displayMetrics.density
        val (rawWidth, rawHeight) = widgetSizePx(context, appWidgetId, config)
        val (widthPx, heightPx) = WidgetBitmap.cappedSize(rawWidth, rawHeight)
        val background = WidgetBitmap.forRemoteViews(
            source = WidgetBackgroundStore.loadForWidget(context, config, widthPx, heightPx),
            radiusPx = radiusPx,
            widthPx = widthPx,
            heightPx = heightPx
        )
        if (background != null) {
            views.setImageViewBitmap(R.id.widget_background, background)
        }
        val overlay = (config.backgroundOpacity.coerceIn(0f, 1f) * 255).toInt()
        views.setInt(R.id.widget_scrim, "setBackgroundColor", Color.argb(overlay, 0, 0, 0))
        views.setTextViewText(R.id.widget_empty, context.getString(emptyTextRes))
        views.setTextColor(
            R.id.widget_empty,
            WidgetBitmap.parseColor(config.textColorHex, 0xFFFFFFFF.toInt())
        )
        val adapter = Intent(context, WidgetListService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse("breez://widget/$appWidgetId")
        }
        views.setRemoteAdapter(R.id.widget_list, adapter)
        views.setEmptyView(R.id.widget_list, R.id.widget_empty)
        val clickFlags = PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag()
        val clickIntent = Intent(context, clickReceiver).apply {
            action = clickAction
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        views.setPendingIntentTemplate(
            R.id.widget_list,
            PendingIntent.getBroadcast(context, appWidgetId, clickIntent, clickFlags)
        )
        val openApp = PendingIntent.getActivity(
            context,
            appWidgetId + 10_000,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )
        views.setOnClickPendingIntent(R.id.widget_empty, openApp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setViewOutlinePreferredRadius(
                R.id.widget_root,
                config.cornerRadiusDp.coerceIn(0, 32).toFloat(),
                TypedValue.COMPLEX_UNIT_DIP
            )
        }
        return views
    }

    fun fallback(context: Context, emptyTextRes: Int): RemoteViews {
        return RemoteViews(context.packageName, R.layout.breez_widget).apply {
            setTextViewText(R.id.widget_empty, context.getString(emptyTextRes))
        }
    }

    private fun widgetSizePx(context: Context, appWidgetId: Int, config: WidgetConfig): Pair<Int, Int> {
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        val density = context.resources.displayMetrics.density
        val widthDp = maxOf(
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0),
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        ).takeIf { it > 0 } ?: (config.cellWidth * 70)
        val heightDp = maxOf(
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0),
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        ).takeIf { it > 0 } ?: (config.cellHeight * 70)
        return (widthDp * density).toInt().coerceAtLeast(1) to
            (heightDp * density).toInt().coerceAtLeast(1)
    }

    private fun mutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
    }

    private fun immutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }
}
