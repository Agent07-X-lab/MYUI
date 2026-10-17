package com.jndapp.minimal.widgets.widgets.battery

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.R
import com.jndapp.minimal.widgets.widgets.WidgetDimensions
import com.jndapp.minimal.widgets.widgets.WidgetRatio
import com.jndapp.minimal.widgets.widgets.WidgetScaleHelper

open class GenericBatteryWidget(
    private val layoutResId: Int,
    private val ratio: WidgetRatio = WidgetRatio.SQUARE
) : BaseBatteryWidget() {

    override fun getLayoutId(): Int = layoutResId
    override fun getWidgetRatio(): WidgetRatio = ratio

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

        val targetPx = dimensions.targetDimensionPx
        val innerRingPx = (0.78f * targetPx).toInt()
        WidgetScaleHelper.setViewSizePx(views, R.id.ring_container_inner, innerRingPx.toFloat())

        try {
            views.setProgressBar(R.id.circular_progress, 100, batteryPercent, false)
        } catch (_: Exception) {}

        val textSizePct = (innerRingPx * 0.32f).coerceAtLeast(18f)
        try {
            val percentBitmap = drawTextBitmap(
                context = context,
                text = "$batteryPercent%",
                textSize = textSizePct,
                textColor = Color.WHITE,
                fontPath = "font/mainb.otf"
            )
            views.setImageViewBitmap(R.id.battery_percentage_image, percentBitmap)
        } catch (_: Exception) {}

        val textSizeStatus = (innerRingPx * 0.14f).coerceIn(10f, 22f)
        try {
            val statusText = if (isCharging) "CHARGING" else "BATTERY"
            val statusBitmap = drawTextBitmap(
                context = context,
                text = statusText,
                textSize = textSizeStatus,
                textColor = accentColor,
                fontPath = "font/mainr.otf"
            )
            views.setImageViewBitmap(R.id.battery_status_image, statusBitmap)
        } catch (_: Exception) {}
    }
}

class Battery2 : GenericBatteryWidget(R.layout.battery2_layout)
class Battery3 : GenericBatteryWidget(R.layout.battery3_layout)
class Battery4 : GenericBatteryWidget(R.layout.battery4_layout, WidgetRatio.GRID_2X1)
class Battery5 : GenericBatteryWidget(R.layout.battery5_layout)
class Battery6 : GenericBatteryWidget(R.layout.battery6_layout)
class Battery7 : GenericBatteryWidget(R.layout.battery7_layout)
class Battery8 : GenericBatteryWidget(R.layout.battery8_layout, WidgetRatio.GRID_1X2)
class Battery9 : GenericBatteryWidget(R.layout.battery9_layout, WidgetRatio.GRID_2X1)
class Battery10 : GenericBatteryWidget(R.layout.battery10_layout)
class Battery11 : GenericBatteryWidget(R.layout.battery11_layout, WidgetRatio.GRID_2X1)
class Battery12 : GenericBatteryWidget(R.layout.battery12_layout)
class Battery13 : GenericBatteryWidget(R.layout.battery13_layout)
class Battery14 : GenericBatteryWidget(R.layout.battery14_layout)
class Battery15 : GenericBatteryWidget(R.layout.widget_battery15, WidgetRatio.GRID_2X2)
class Battery16 : GenericBatteryWidget(R.layout.battery16_layout)
class Battery17 : GenericBatteryWidget(R.layout.battery17_layout, WidgetRatio.GRID_4X2)
class Battery18 : GenericBatteryWidget(R.layout.battery18_layout)
class Battery19 : GenericBatteryWidget(R.layout.battery19_layout)
