package com.jndapp.minimal.widgets.widgets.digital

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.provider.AlarmClock
import android.text.format.DateFormat
import android.util.Log
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.MainActivity
import com.jndapp.minimal.widgets.R
import com.jndapp.minimal.widgets.widgets.WidgetRatio
import com.jndapp.minimal.widgets.widgets.WidgetScaleHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

abstract class DigitalBase : AppWidgetProvider() {

    companion object {
        private const val TAG = "DigitalBase"
        private val typefaceCache = ConcurrentHashMap<String, Typeface>()

        fun getTypeface(context: Context, fontPath: String): Typeface {
            return typefaceCache.getOrPut(fontPath) {
                try {
                    Typeface.createFromAsset(context.applicationContext.assets, fontPath)
                } catch (e: Exception) {
                    Log.w(TAG, "Error loading font $fontPath, falling back to DEFAULT", e)
                    Typeface.DEFAULT_BOLD
                }
            }
        }
    }

    open fun getTimeFont(): String = "font/iwidget_01.otf"
    open fun getDateFont(): String = "font/mainb.otf"
    open fun getTimeScale(): Float = 1.0f
    open fun getDateScale(): Float = 0.22f
    open fun getSpacing(): Float = 0.05f
    open fun getTimeStyle(): String = "none" // "none", "colon", "dot", "newline"
    open fun getLayoutId(): Int = R.layout.digital_layout

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleNextUpdate(context)
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

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action == Intent.ACTION_TIME_TICK ||
            action == Intent.ACTION_TIME_SET ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_USER_PRESENT ||
            action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, javaClass))
            if (ids != null && ids.isNotEmpty()) {
                for (id in ids) {
                    updateWidget(context, appWidgetManager, id)
                }
            }
            scheduleNextUpdate(context)
        }
    }

    open fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            val views = RemoteViews(context.getPackageName(), getLayoutId())

            val dimensions = WidgetScaleHelper.applyWidgetScale(
                context = context,
                views = views,
                appWidgetManager = appWidgetManager,
                appWidgetId = appWidgetId,
                rootViewId = R.id.widget_root,
                ratio = WidgetRatio.GRID_2X1
            )

            val density = dimensions.density
            if (dimensions.widthPaddingPx == 0 && dimensions.heightPaddingPx == 0) {
                val safeMargin = (6 * density).toInt()
                try {
                    views.setViewPadding(R.id.widget_root, safeMargin, safeMargin, safeMargin, safeMargin)
                } catch (e: Exception) {
                    Log.w(TAG, "Error setting padding: ${e.message}")
                }
            }

            val calendar = Calendar.getInstance()
            val is24Hour = DateFormat.is24HourFormat(context)

            val hourPattern = if (is24Hour) "HH" else "hh"
            val hourStr = SimpleDateFormat(hourPattern, Locale.getDefault()).format(calendar.time)
            val minStr = SimpleDateFormat("mm", Locale.getDefault()).format(calendar.time)

            val separator = when (getTimeStyle()) {
                "colon" -> ":"
                "dot" -> "."
                "newline" -> "\n"
                else -> ":"
            }
            val timeText = "$hourStr$separator$minStr"
            val dateText = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(calendar.time)

            val targetW = (dimensions.widthDp * density * 1.5f).toInt().coerceAtLeast(300)
            val targetH = (dimensions.heightDp * density * 1.5f).toInt().coerceAtLeast(150)

            val textColor = getResolvedTextColor(context)
            val timeBitmap = renderClockBitmap(
                context = context,
                timeText = timeText,
                dateText = dateText,
                widthPx = targetW,
                heightPx = targetH,
                textColor = textColor,
                timeScale = getTimeScale(),
                dateScale = getDateScale(),
                spacing = getSpacing(),
                timeFontPath = getTimeFont(),
                dateFontPath = getDateFont()
            )

            views.setImageViewBitmap(R.id.widget_content, timeBitmap)

            // Clock launch intent
            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent = if (clockIntent.resolveActivity(context.packageManager) != null) {
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    clockIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            views.setOnClickPendingIntent(R.id.widget_content, pendingIntent)
            try {
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            } catch (ignored: Exception) {
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating digital widget $appWidgetId", e)
        }
    }

    private fun getResolvedTextColor(context: Context): Int {
        val isDark = (context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        return if (isDark) Color.WHITE else Color.parseColor("#111111")
    }

    private fun renderClockBitmap(
        context: Context,
        timeText: String,
        dateText: String,
        widthPx: Int,
        heightPx: Int,
        textColor: Int,
        timeScale: Float,
        dateScale: Float,
        spacing: Float,
        timeFontPath: String,
        dateFontPath: String
    ): Bitmap {
        val timeTypeface = getTypeface(context, timeFontPath)
        val dateTypeface = getTypeface(context, dateFontPath)

        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = timeTypeface
            color = textColor
            textAlign = Paint.Align.CENTER
            isSubpixelText = true
            isFilterBitmap = true
            isDither = true
        }

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = dateTypeface
            color = textColor
            alpha = 190
            textAlign = Paint.Align.CENTER
            isSubpixelText = true
            isFilterBitmap = true
            isDither = true
        }

        // Determine font size to fit width and height with headroom
        var candidateTimeSize = (heightPx * 0.52f * timeScale).coerceAtLeast(24f)
        timePaint.textSize = candidateTimeSize
        val timeBounds = Rect()
        timePaint.getTextBounds(timeText, 0, timeText.length, timeBounds)

        // Scale down if timeText exceeds 88% of width
        val maxWidth = widthPx * 0.88f
        if (timeBounds.width() > maxWidth && timeBounds.width() > 0) {
            candidateTimeSize *= (maxWidth / timeBounds.width())
            timePaint.textSize = candidateTimeSize
            timePaint.getTextBounds(timeText, 0, timeText.length, timeBounds)
        }

        val dateSize = (candidateTimeSize * dateScale).coerceAtLeast(14f)
        datePaint.textSize = dateSize
        val dateBounds = Rect()
        datePaint.getTextBounds(dateText, 0, dateText.length, dateBounds)

        val spacingPx = candidateTimeSize * spacing
        val totalContentHeight = timeBounds.height() + spacingPx + dateBounds.height()

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val centerX = widthPx / 2f
        val startY = (heightPx - totalContentHeight) / 2f

        val timeBaseline = startY + timeBounds.height()
        canvas.drawText(timeText, centerX, timeBaseline, timePaint)

        val dateBaseline = timeBaseline + spacingPx + dateBounds.height()
        canvas.drawText(dateText, centerX, dateBaseline, datePaint)

        return bitmap
    }

    private fun scheduleNextUpdate(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val calendar = Calendar.getInstance().apply {
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, 1)
            }
            val intent = Intent(context, javaClass).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to schedule next minute alarm: ${e.message}")
        }
    }
}
