package com.jndapp.minimal.widgets.widgets.analog

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

abstract class BaseAnalogWidget : AppWidgetProvider() {

    abstract fun getLayoutId(): Int

    abstract fun configureViews(context: Context, views: RemoteViews, appWidgetManager: AppWidgetManager)

    fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, getLayoutId())
        configureViews(context, views, appWidgetManager)

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

    companion object {
        fun getFormattedDayDate(): String {
            val now = LocalDate.now()
            val dayName = now.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
            return "$dayName ${now.dayOfMonth}"
        }
    }
}
