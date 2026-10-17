package com.jndapp.minimal.widgets.widgets.analog

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.R
import com.jndapp.minimal.widgets.widgets.WidgetDimensions
import com.jndapp.minimal.widgets.widgets.WidgetRatio
import com.jndapp.minimal.widgets.widgets.WidgetScaleHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

abstract class BaseAnalogWidget : AppWidgetProvider() {

    abstract fun getLayoutId(): Int

    abstract fun configureViews(
        context: Context,
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        dimensions: WidgetDimensions
    )

    fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, getLayoutId())

        // Calculate and apply scaling / padding based on launcher cell bounds
        val dimensions = WidgetScaleHelper.applyWidgetScale(
            context = context,
            views = views,
            appWidgetManager = appWidgetManager,
            appWidgetId = appWidgetId,
            rootViewId = R.id.widget_root,
            ratio = WidgetRatio.SQUARE
        )

        configureViews(context, views, appWidgetManager, dimensions)

        // Launch system clock / alarms on tap
        val clockIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName("com.google.android.deskclock", "com.android.deskclock.DeskClock")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            clockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        try {
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            views.setOnClickPendingIntent(R.id.analog_clock, pendingIntent)
        } catch (_: Exception) {}

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val editor = context.getSharedPreferences("widget_ratio_preferences", Context.MODE_PRIVATE).edit()
        for (id in appWidgetIds) {
            editor.remove("widget_fixed_ratio_$id")
        }
        editor.apply()
    }

    companion object {
        fun getFormattedDayDate(): String {
            val now = LocalDate.now()
            val dayName = now.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
            return "$dayName ${now.dayOfMonth}"
        }
    }
}
