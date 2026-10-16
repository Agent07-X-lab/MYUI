package com.jndapp.minimal.widgets.widgets.battery

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.R

class Battery1 : BaseBatteryWidget() {

    override fun getLayoutId(): Int = R.layout.battery1_layout

    override fun updateBatteryViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        batteryPercent: Int,
        isCharging: Boolean
    ) {
        val views = RemoteViews(context.packageName, getLayoutId())
        val studioPrefs = context.getSharedPreferences("studio_prefs", Context.MODE_PRIVATE)
        val widgetPrefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)

        val accentColor = studioPrefs.getInt("theme_accent_color", Color.parseColor("#FFBF00"))
        val bgColor = studioPrefs.getInt("theme_background_color", Color.parseColor("#000000"))
        val opacity = widgetPrefs.getFloat("widget_opacity", 1.0f)

        views.setInt(R.id.widget_container, "setBackgroundColor", bgColor)
        views.setFloat(R.id.widget_container, "setAlpha", opacity.coerceIn(0.0f, 1.0f))

        views.setProgressBar(R.id.circular_progress, 100, batteryPercent, false)

        val percentBitmap = drawTextBitmap(
            context = context,
            text = "$batteryPercent%",
            textSize = 34f,
            textColor = Color.WHITE,
            fontPath = "font/mainb.otf"
        )
        views.setImageViewBitmap(R.id.battery_percentage_image, percentBitmap)

        val statusText = if (isCharging) "CHARGING" else "BATTERY"
        val statusBitmap = drawTextBitmap(
            context = context,
            text = statusText,
            textSize = 14f,
            textColor = accentColor,
            fontPath = "font/mainr.otf"
        )
        views.setImageViewBitmap(R.id.battery_status_image, statusBitmap)

        val powerIntent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            powerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
