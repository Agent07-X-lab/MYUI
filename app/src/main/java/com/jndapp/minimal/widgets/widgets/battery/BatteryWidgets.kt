package com.jndapp.minimal.widgets.widgets.battery

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.R

open class GenericBatteryWidget(private val layoutResId: Int) : BaseBatteryWidget() {
    override fun getLayoutId(): Int = layoutResId

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

        try {
            views.setProgressBar(R.id.circular_progress, 100, batteryPercent, false)
        } catch (_: Exception) {}

        try {
            val percentBitmap = drawTextBitmap(
                context = context,
                text = "$batteryPercent%",
                textSize = 34f,
                textColor = Color.WHITE,
                fontPath = "font/mainb.otf"
            )
            views.setImageViewBitmap(R.id.battery_percentage_image, percentBitmap)
        } catch (_: Exception) {}

        try {
            val statusText = if (isCharging) "CHARGING" else "BATTERY"
            val statusBitmap = drawTextBitmap(
                context = context,
                text = statusText,
                textSize = 14f,
                textColor = accentColor,
                fontPath = "font/mainr.otf"
            )
            views.setImageViewBitmap(R.id.battery_status_image, statusBitmap)
        } catch (_: Exception) {}

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

class Battery2 : GenericBatteryWidget(R.layout.battery2_layout)
class Battery3 : GenericBatteryWidget(R.layout.battery3_layout)
class Battery4 : GenericBatteryWidget(R.layout.battery4_layout)
class Battery5 : GenericBatteryWidget(R.layout.battery5_layout)
class Battery6 : GenericBatteryWidget(R.layout.battery6_layout)
class Battery7 : GenericBatteryWidget(R.layout.battery7_layout)
class Battery8 : GenericBatteryWidget(R.layout.battery8_layout)
class Battery9 : GenericBatteryWidget(R.layout.battery9_layout)
class Battery10 : GenericBatteryWidget(R.layout.battery10_layout)
class Battery11 : GenericBatteryWidget(R.layout.battery11_layout)
class Battery12 : GenericBatteryWidget(R.layout.battery12_layout)
class Battery13 : GenericBatteryWidget(R.layout.battery13_layout)
class Battery14 : GenericBatteryWidget(R.layout.battery14_layout)
class Battery15 : GenericBatteryWidget(R.layout.battery15_layout)
class Battery16 : GenericBatteryWidget(R.layout.battery16_layout)
class Battery17 : GenericBatteryWidget(R.layout.battery17_layout)
class Battery18 : GenericBatteryWidget(R.layout.battery18_layout)
class Battery19 : GenericBatteryWidget(R.layout.battery19_layout)
class Battery20 : GenericBatteryWidget(R.layout.battery20_layout)
