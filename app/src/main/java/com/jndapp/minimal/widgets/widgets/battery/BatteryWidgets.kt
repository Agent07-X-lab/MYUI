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
        try {
            WidgetScaleHelper.setViewSizePx(views, R.id.ring_container_inner, innerRingPx.toFloat())
        } catch (_: Exception) {}

        // 1. All progress bars across battery 1..19
        try { views.setProgressBar(R.id.circular_progress, 100, batteryPercent, false) } catch (_: Exception) {}
        try { views.setProgressBar(R.id.progress_bar, 100, batteryPercent, false) } catch (_: Exception) {}
        try { views.setProgressBar(R.id.battery_progress, 100, batteryPercent, false) } catch (_: Exception) {}
        try { views.setProgressBar(R.id.battery_progress_bar, 100, batteryPercent, false) } catch (_: Exception) {}
        try { views.setProgressBar(R.id.apple_progress, 100, batteryPercent, false) } catch (_: Exception) {}
        try { views.setProgressBar(R.id.battery_level_indicator, 100, batteryPercent, false) } catch (_: Exception) {}
        try { views.setProgressBar(R.id.mobile_circular_progress, 100, batteryPercent, false) } catch (_: Exception) {}
        try { views.setProgressBar(R.id.earbuds_circular_progress, 100, (batteryPercent - 5).coerceAtLeast(15), false) } catch (_: Exception) {}

        // 2. Direct TextViews
        val statusText = if (isCharging) "CHARGING" else "BATTERY"
        try {
            views.setTextViewText(R.id.battery_percentage, "$batteryPercent%")
            views.setTextColor(R.id.battery_percentage, Color.WHITE)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.battery_percentage_text, "$batteryPercent%")
            views.setTextColor(R.id.battery_percentage_text, Color.WHITE)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.battery_status, statusText)
            views.setTextColor(R.id.battery_status, accentColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.battery_status_text, statusText)
            views.setTextColor(R.id.battery_status_text, accentColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.status_text, statusText)
            views.setTextColor(R.id.status_text, accentColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.battery_time_remaining, if (isCharging) "Fast Charging" else "Remaining")
        } catch (_: Exception) {}

        // 3. Digital Clock in Battery17
        try {
            val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            views.setTextViewText(R.id.digital_clock, timeStr)
        } catch (_: Exception) {}

        // 4. Charge icon visibility
        try {
            views.setViewVisibility(R.id.charge_icon, if (isCharging) android.view.View.VISIBLE else android.view.View.GONE)
        } catch (_: Exception) {}

        // 5. Image-based text views
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
            views.setImageViewBitmap(R.id.mobile_percentage_image, percentBitmap)
        } catch (_: Exception) {}

        try {
            val earbudsPctBitmap = drawTextBitmap(
                context = context,
                text = "${(batteryPercent - 5).coerceAtLeast(15)}%",
                textSize = textSizePct * 0.85f,
                textColor = Color.WHITE,
                fontPath = "font/mainb.otf"
            )
            views.setImageViewBitmap(R.id.earbuds_percentage_image, earbudsPctBitmap)
        } catch (_: Exception) {}

        val textSizeStatus = (innerRingPx * 0.14f).coerceIn(10f, 22f)
        try {
            val statusBitmap = drawTextBitmap(
                context = context,
                text = statusText,
                textSize = textSizeStatus,
                textColor = accentColor,
                fontPath = "font/mainr.otf"
            )
            views.setImageViewBitmap(R.id.battery_status_image, statusBitmap)
        } catch (_: Exception) {}

        // 6. Custom battery dial bitmap for Battery18 & Battery19
        try {
            val dialBitmap = drawBatteryDialBitmap(context, batteryPercent, isCharging, accentColor)
            views.setImageViewBitmap(R.id.battery_content_image, dialBitmap)
        } catch (_: Exception) {}
    }

    private fun drawBatteryDialBitmap(
        context: Context,
        percent: Int,
        isCharging: Boolean,
        accentColor: Int,
        sizePx: Int = 360
    ): android.graphics.Bitmap {
        val bitmap = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = sizePx * 0.38f

        // Background track
        val trackPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = sizePx * 0.08f
            strokeCap = android.graphics.Paint.Cap.ROUND
            color = android.graphics.Color.argb(50, 255, 255, 255)
        }
        val rect = android.graphics.RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        canvas.drawArc(rect, 135f, 270f, false, trackPaint)

        // Progress arc
        val sweep = 270f * (percent.coerceIn(0, 100) / 100f)
        val progressPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = sizePx * 0.08f
            strokeCap = android.graphics.Paint.Cap.ROUND
            color = accentColor
        }
        if (sweep > 0) {
            canvas.drawArc(rect, 135f, sweep, false, progressPaint)
        }

        // Center text percentage
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = sizePx * 0.22f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val bounds = android.graphics.Rect()
        val pctStr = "$percent%"
        textPaint.getTextBounds(pctStr, 0, pctStr.length, bounds)
        canvas.drawText(pctStr, cx, cy - bounds.exactCenterY() - (sizePx * 0.03f), textPaint)

        // Subtext (CHARGING or BATTERY)
        val subPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = sizePx * 0.09f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val subText = if (isCharging) "CHARGING" else "BATTERY"
        canvas.drawText(subText, cx, cy + (sizePx * 0.18f), subPaint)

        return bitmap
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
