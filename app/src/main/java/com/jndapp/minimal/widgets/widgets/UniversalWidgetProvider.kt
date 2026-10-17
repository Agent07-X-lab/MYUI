package com.jndapp.minimal.widgets.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.MainActivity
import com.jndapp.minimal.widgets.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

open class UniversalWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "UniversalWidgetProvider"
    }

    open fun getCustomLayoutId(): Int = 0
    open fun getCustomRatio(): WidgetRatio = WidgetRatio.SQUARE

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

    open fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
            val layoutId = if (getCustomLayoutId() != 0) {
                getCustomLayoutId()
            } else {
                info?.initialLayout ?: 0
            }

            if (layoutId == 0) {
                Log.w(TAG, "No initial layout found for widget $appWidgetId")
                return
            }

            val views = RemoteViews(context.packageName, layoutId)

            val dimensions = WidgetScaleHelper.applyWidgetScale(
                context = context,
                views = views,
                appWidgetManager = appWidgetManager,
                appWidgetId = appWidgetId,
                rootViewId = R.id.widget_root,
                ratio = getCustomRatio()
            )

            val density = dimensions.density
            if (dimensions.widthPaddingPx == 0 && dimensions.heightPaddingPx == 0) {
                val safeMargin = (6 * density).toInt()
                try {
                    views.setViewPadding(R.id.widget_root, safeMargin, safeMargin, safeMargin, safeMargin)
                } catch (ignored: Exception) {
                }
            }

            val isDark = (context.resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES

            val bgColor = if (isDark) Color.parseColor("#1E1E1E") else Color.parseColor("#F5F5F7")
            val primaryTextColor = if (isDark) Color.WHITE else Color.parseColor("#111111")
            val secondaryTextColor = if (isDark) Color.parseColor("#B0B0B0") else Color.parseColor("#666666")

            try {
                views.setColorStateList(R.id.widget_container, "setBackgroundTintList", ColorStateList.valueOf(bgColor))
            } catch (ignored: Exception) {
            }

            // Populate time / date if text views exist
            val calendar = Calendar.getInstance()
            val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
            val dayText = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time).uppercase()
            val dateText = SimpleDateFormat("d MMM", Locale.getDefault()).format(calendar.time)

            try {
                views.setTextViewText(R.id.time_display, timeText)
                views.setTextColor(R.id.time_display, primaryTextColor)
            } catch (ignored: Exception) {
            }

            try {
                views.setTextViewText(R.id.date_day_display, "$dayText, $dateText")
                views.setTextColor(R.id.date_day_display, secondaryTextColor)
            } catch (ignored: Exception) {
            }

            // Default click intent -> launch MainActivity or specialized action
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            } catch (ignored: Exception) {
            }

            try {
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            } catch (ignored: Exception) {
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating universal widget $appWidgetId", e)
        }
    }
}

class TestWidget : UniversalWidgetProvider()
class WidgetPinnedReceiver : UniversalWidgetProvider()
