package com.jndapp.minimal.widgets.widgets.battery

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.BatteryManager
import android.os.Bundle
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.R

abstract class BaseBatteryWidget : AppWidgetProvider() {

    abstract fun getLayoutId(): Int

    abstract fun updateBatteryViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        batteryPercent: Int,
        isCharging: Boolean
    )

    fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 50
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 50

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        updateBatteryViews(context, appWidgetManager, appWidgetId, batteryPct, isCharging)
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
        fun drawTextBitmap(
            context: Context,
            text: String,
            textSize: Float,
            textColor: Int,
            fontPath: String = "font/mainb.otf"
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
            val width = (bounds.width() + 16).coerceAtLeast(1)
            val height = (bounds.height() + 16).coerceAtLeast(1)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawText(text, width / 2f, height / 2f - bounds.exactCenterY(), paint)
            return bitmap
        }
    }
}
