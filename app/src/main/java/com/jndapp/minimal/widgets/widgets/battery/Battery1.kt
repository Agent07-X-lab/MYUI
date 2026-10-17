package com.jndapp.minimal.widgets.widgets.battery

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.R
import com.jndapp.minimal.widgets.widgets.WidgetDimensions
import com.jndapp.minimal.widgets.widgets.WidgetScaleHelper

class Battery1 : BaseBatteryWidget() {

    override fun getLayoutId(): Int = R.layout.battery1_layout

    override fun updateBatteryViews(
        context: Context,
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        batteryPercent: Int,
        isCharging: Boolean,
        dimensions: WidgetDimensions
    ) {
        val studioPrefs = context.getSharedPreferences("studio_prefs", Context.MODE_PRIVATE)
        val widgetPrefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)

        val accentColor = studioPrefs.getInt("theme_accent_color", Color.parseColor("#FFBF00"))
        val bgColor = studioPrefs.getInt("theme_background_color", Color.parseColor("#000000"))
        val opacity = widgetPrefs.getFloat("widget_opacity", 1.0f)

        try {
            views.setColorStateList(R.id.widget_container, "setBackgroundTintList", ColorStateList.valueOf(bgColor))
        } catch (_: Exception) {
            views.setInt(R.id.widget_container, "setBackgroundColor", bgColor)
        }
        views.setFloat(R.id.widget_container, "setAlpha", opacity.coerceIn(0.0f, 1.0f))

        // Dynamically scale inner ring container so the circular progress bar fits any cell dimension
        val targetPx = dimensions.targetDimensionPx
        val innerRingPx = (0.78f * targetPx).toInt()
        WidgetScaleHelper.setViewSizePx(views, R.id.ring_container_inner, innerRingPx.toFloat())

        try {
            views.setProgressBar(R.id.circular_progress, 100, batteryPercent, false)
        } catch (_: Exception) {}

        val textSizePct = (innerRingPx * 0.32f).coerceAtLeast(18f)
        val percentBitmap = drawTextBitmap(
            context = context,
            text = "$batteryPercent%",
            textSize = textSizePct,
            textColor = Color.WHITE,
            fontPath = "font/mainb.otf"
        )
        views.setImageViewBitmap(R.id.battery_percentage_image, percentBitmap)

        val textSizeStatus = (innerRingPx * 0.14f).coerceIn(10f, 22f)
        val statusText = if (isCharging) "CHARGING" else "BATTERY"
        val statusBitmap = drawTextBitmap(
            context = context,
            text = statusText,
            textSize = textSizeStatus,
            textColor = accentColor,
            fontPath = "font/mainr.otf"
        )
        views.setImageViewBitmap(R.id.battery_status_image, statusBitmap)
    }
}
