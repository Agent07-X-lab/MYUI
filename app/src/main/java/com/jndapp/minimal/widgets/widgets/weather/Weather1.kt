package com.jndapp.minimal.widgets.widgets.weather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.R
import com.jndapp.minimal.widgets.widgets.WidgetRatio
import com.jndapp.minimal.widgets.widgets.WidgetScaleHelper

class Weather1 : WeatherBase() {

    override fun updateWeatherWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_weather_1)

        val dimensions = WidgetScaleHelper.applyWidgetScale(
            context = context,
            views = views,
            appWidgetManager = appWidgetManager,
            appWidgetId = appWidgetId,
            rootViewId = R.id.widget_root,
            ratio = WidgetRatio.SQUARE
        )

        val studioPrefs = context.getSharedPreferences("studio_prefs", Context.MODE_PRIVATE)
        val widgetPrefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        val weatherPrefs = context.getSharedPreferences("WeatherWidgetPrefs", Context.MODE_PRIVATE)

        val accentColor = studioPrefs.getInt("theme_accent_color", Color.parseColor("#FFBF00"))
        val bgColor = studioPrefs.getInt("theme_background_color", Color.parseColor("#000000"))
        val opacity = widgetPrefs.getFloat("widget_opacity", 1.0f)

        try {
            views.setColorStateList(R.id.widget_container, "setBackgroundTintList", ColorStateList.valueOf(bgColor))
        } catch (_: Exception) {
            views.setInt(R.id.widget_container, "setBackgroundColor", bgColor)
        }
        views.setFloat(R.id.widget_container, "setAlpha", opacity.coerceIn(0.0f, 1.0f))

        val cityName = weatherPrefs.getString("city_$appWidgetId", null)
        val isConfigured = !cityName.isNullOrBlank()

        val tempText = if (isConfigured) "24°" else "Config"
        val locationText = if (isConfigured) "in ${cityName?.lowercase()}" else "tap to setup"

        val targetPx = dimensions.targetDimensionPx
        val tempSize = (targetPx * 0.22f).coerceIn(24f, 54f)
        val citySize = (targetPx * 0.11f).coerceIn(12f, 26f)

        try {
            val tempBitmap = drawTextBitmap(
                context = context,
                text = tempText,
                textSize = tempSize,
                textColor = Color.WHITE,
                fontPath = "font/mainb.otf"
            )
            views.setImageViewBitmap(R.id.temperature, tempBitmap)
        } catch (_: Exception) {}

        try {
            val cityBitmap = drawTextBitmap(
                context = context,
                text = locationText,
                textSize = citySize,
                textColor = accentColor,
                fontPath = "font/mainr.otf"
            )
            views.setImageViewBitmap(R.id.city_name, cityBitmap)
        } catch (_: Exception) {}

        try {
            views.setImageViewResource(R.id.weather_icon, R.drawable.ic_weather_sunny)
            views.setProgressBar(R.id.progress_bar, 100, 60, false)
        } catch (_: Exception) {}

        // Launch WeatherWidgetConfigActivity on tap
        val configIntent = Intent(context, WeatherWidgetConfigActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            configIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun drawTextBitmap(
        context: Context,
        text: String,
        textSize: Float,
        textColor: Int,
        fontPath: String
    ): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            color = textColor
            textAlign = Paint.Align.CENTER
            try {
                typeface = Typeface.createFromAsset(context.assets, fontPath)
            } catch (_: Exception) {}
        }

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val width = (bounds.width() + 24).coerceAtLeast(1)
        val height = (bounds.height() + 24).coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, width / 2f, height / 2f - bounds.exactCenterY(), paint)
        return bitmap
    }
}
