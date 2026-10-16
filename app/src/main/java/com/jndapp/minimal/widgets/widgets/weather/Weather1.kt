package com.jndapp.minimal.widgets.widgets.weather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.R

class Weather1 : WeatherBase() {

    override fun updateWeatherWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_weather_1)
        val studioPrefs = context.getSharedPreferences("studio_prefs", Context.MODE_PRIVATE)
        val widgetPrefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)

        val accentColor = studioPrefs.getInt("theme_accent_color", Color.parseColor("#FFBF00"))
        val bgColor = studioPrefs.getInt("theme_background_color", Color.parseColor("#000000"))
        val opacity = widgetPrefs.getFloat("widget_opacity", 1.0f)

        try {
            views.setInt(R.id.widget_container, "setBackgroundColor", bgColor)
            views.setFloat(R.id.widget_container, "setAlpha", opacity.coerceIn(0.0f, 1.0f))
        } catch (_: Exception) {}

        try {
            views.setTextColor(R.id.temperature, Color.WHITE)
            views.setTextViewText(R.id.temperature, "24°")
        } catch (_: Exception) {}

        try {
            views.setTextColor(R.id.weather_condition, accentColor)
            views.setTextViewText(R.id.weather_condition, "Sunny")
        } catch (_: Exception) {}

        // Launch system weather / browser on tap
        val weatherIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://weather.com"))
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            weatherIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        } catch (_: Exception) {}

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
