package com.breez.notes.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.breez.notes.R
import com.breez.notes.di.WidgetEntryPoint
import com.breez.notes.domain.model.WidgetConfig
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking

class WidgetListService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        return WidgetListFactory(applicationContext, appWidgetId)
    }
}

class WidgetListFactory(
    private val context: Context,
    private val appWidgetId: Int
) : RemoteViewsService.RemoteViewsFactory {

    private var rows: List<WidgetRow> = emptyList()
    private var config: WidgetConfig = WidgetConfig.default(appWidgetId)

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        rows = runBlocking {
            val entry = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val kind = WidgetKinds.of(context, appWidgetId)
            config = entry.widgetRepository().getByAppWidgetId(appWidgetId)
                ?: defaultWidgetConfig(appWidgetId, kind)
            WidgetData.buildRows(WidgetData.loadNotes(entry, config, kind), config, kind)
        }
    }

    override fun onDestroy() {
        rows = emptyList()
    }

    override fun getCount(): Int = rows.size

    override fun getViewAt(position: Int): RemoteViews {
        val fallback = RemoteViews(context.packageName, R.layout.breez_widget_item)
        val row = rows.getOrNull(position) ?: return fallback
        return runCatching { bindRow(row) }.getOrDefault(fallback)
    }

    private fun bindRow(row: WidgetRow): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.breez_widget_item)
        val textColor = WidgetBitmap.parseColor(config.textColorHex, 0xFFFFFFFF.toInt())
        val fontSize = config.fontSizeSp.coerceIn(10, 24).toFloat()
        val mark = when {
            !row.toggleable -> ""
            row.done -> "☑"
            else -> "☐"
        }
        views.setTextViewText(R.id.widget_item_mark, mark)
        views.setTextColor(R.id.widget_item_mark, textColor)
        views.setTextViewTextSize(R.id.widget_item_mark, TypedValue.COMPLEX_UNIT_SP, fontSize)
        val text = if (row.body.isNullOrBlank()) row.title else "${row.title}\n${row.body}"
        val anim = WidgetStrikeAnimator.current()
        val animating = WidgetStrikeAnimator.isAnimating(appWidgetId, row.noteId, row.lineIndex)
        if (animating && anim != null) {
            views.setViewVisibility(R.id.widget_item_text, View.GONE)
            views.setViewVisibility(R.id.widget_item_strike, View.VISIBLE)
            val density = context.resources.displayMetrics.density
            val bitmap = WidgetBitmap.strikeText(
                text = text,
                color = textColor,
                textSizePx = fontSize * density,
                widthPx = (220 * density).toInt(),
                progress = anim.progress
            )
            views.setImageViewBitmap(R.id.widget_item_strike, bitmap)
        } else {
            views.setViewVisibility(R.id.widget_item_strike, View.GONE)
            views.setViewVisibility(R.id.widget_item_text, View.VISIBLE)
            views.setTextViewText(R.id.widget_item_text, text)
            views.setTextColor(R.id.widget_item_text, textColor)
            views.setTextViewTextSize(R.id.widget_item_text, TypedValue.COMPLEX_UNIT_SP, fontSize)
            val flags = if (row.done) {
                Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
            } else {
                Paint.ANTI_ALIAS_FLAG
            }
            views.setInt(R.id.widget_item_text, "setPaintFlags", flags)
        }
        val fill = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(BreezWidgetReceiver.EXTRA_NOTE_ID, row.noteId)
            putExtra(BreezWidgetReceiver.EXTRA_LINE_INDEX, row.lineIndex)
            putExtra(BreezWidgetReceiver.EXTRA_TOGGLEABLE, row.toggleable)
            putExtra(BreezWidgetReceiver.EXTRA_DONE, row.done)
        }
        views.setOnClickFillInIntent(R.id.widget_item_root, fill)
        return views
    }

    override fun getLoadingView(): RemoteViews =
        RemoteViews(context.packageName, R.layout.breez_widget_item)

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = rows.getOrNull(position)?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
