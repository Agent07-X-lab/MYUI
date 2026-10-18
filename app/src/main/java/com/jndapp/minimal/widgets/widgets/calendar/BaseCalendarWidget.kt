package com.jndapp.minimal.widgets.widgets.calendar

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
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

abstract class BaseCalendarWidget : AppWidgetProvider() {

    companion object {
        private const val TAG = "BaseCalendarWidget"
        private val bitmapCache = ConcurrentHashMap<String, Bitmap>()
        private var fontTypeface: Typeface? = null

        fun getFont(context: Context): Typeface {
            if (fontTypeface == null) {
                try {
                    fontTypeface = Typeface.createFromAsset(context.applicationContext.assets, "font/mainb.otf")
                } catch (e: Exception) {
                    fontTypeface = Typeface.DEFAULT_BOLD
                }
            }
            return fontTypeface ?: Typeface.DEFAULT_BOLD
        }

        fun createTextBitmap(
            context: Context,
            text: String,
            textSize: Float,
            textColor: Int,
            padding: Int = 0
        ): Bitmap {
            val key = "$text-$textSize-$textColor-$padding"
            bitmapCache[key]?.let { if (!it.isRecycled) return it }

            try {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = getFont(context)
                    this.textSize = textSize
                    color = textColor
                    textAlign = Paint.Align.LEFT
                    isSubpixelText = true
                    isFilterBitmap = true
                    isDither = true
                }
                val bounds = Rect()
                paint.getTextBounds(text, 0, text.length, bounds)

                val w = (bounds.width() + padding * 2).coerceAtLeast(1)
                val h = (bounds.height() + padding * 2).coerceAtLeast(1)

                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawText(text, (padding - bounds.left).toFloat(), (h - padding - bounds.bottom).toFloat(), paint)

                bitmapCache[key] = bitmap
                return bitmap
            } catch (e: Exception) {
                Log.w(TAG, "Error creating text bitmap for $text", e)
                return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
        }
    }

    open fun getLayoutId(): Int = R.layout.calendar1_layout
    open fun getRatio(): WidgetRatio = WidgetRatio.SQUARE

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
        scheduleMidnightUpdate(context)
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
        if (action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_CONFIGURATION_CHANGED ||
            action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, javaClass))
            if (ids != null && ids.isNotEmpty()) {
                for (id in ids) {
                    updateWidget(context, appWidgetManager, id)
                }
            }
            scheduleMidnightUpdate(context)
        }
    }

    open fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            val views = RemoteViews(context.packageName, getLayoutId())

            val dimensions = WidgetScaleHelper.applyWidgetScale(
                context = context,
                views = views,
                appWidgetManager = appWidgetManager,
                appWidgetId = appWidgetId,
                rootViewId = R.id.widget_root,
                ratio = getRatio()
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
            val accentColor = Color.parseColor("#E53935") // Red accent for day of week / headers

            // Tint container if present
            try {
                views.setColorStateList(R.id.widget_container, "setBackgroundTintList", ColorStateList.valueOf(bgColor))
            } catch (ignored: Exception) {
            }

            renderCalendarContent(context, views, primaryTextColor, accentColor, dimensions.targetDimensionDp)

            // Calendar launch intent
            val calIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALENDAR)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent = if (calIntent.resolveActivity(context.packageManager) != null) {
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    calIntent,
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

            try {
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            } catch (ignored: Exception) {
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating calendar widget $appWidgetId", e)
        }
    }

    open fun renderCalendarContent(
        context: Context,
        views: RemoteViews,
        primaryTextColor: Int,
        accentColor: Int,
        targetDimDp: Int
    ) {
        val calendar = Calendar.getInstance()
        val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time).uppercase()
        val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time).uppercase()
        val fullMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        val dayNumber = calendar.get(Calendar.DAY_OF_MONTH).toString()

        // 1. widget_day & widget_date (Calendar1, Calendar6, etc.)
        try {
            val dayBitmap = createTextBitmap(context, "$dayOfWeek $monthName", 140f, accentColor)
            views.setImageViewBitmap(R.id.widget_day, dayBitmap)
        } catch (ignored: Exception) {
        }

        try {
            val dateBitmap = createTextBitmap(context, dayNumber, 480f, primaryTextColor)
            views.setImageViewBitmap(R.id.widget_date, dateBitmap)
        } catch (ignored: Exception) {
        }

        // 2. Direct TextViews if present
        try {
            views.setTextViewText(R.id.day_name, dayOfWeek)
            views.setTextColor(R.id.day_name, accentColor)
        } catch (ignored: Exception) {
        }

        try {
            views.setTextViewText(R.id.day_name_text, dayOfWeek)
            views.setTextColor(R.id.day_name_text, accentColor)
        } catch (ignored: Exception) {
        }

        try {
            views.setTextViewText(R.id.date_number, dayNumber)
            views.setTextColor(R.id.date_number, primaryTextColor)
        } catch (ignored: Exception) {
        }

        try {
            views.setTextViewText(R.id.day_number_text, dayNumber)
            views.setTextColor(R.id.day_number_text, primaryTextColor)
        } catch (ignored: Exception) {
        }

        try {
            views.setTextViewText(R.id.month_name, monthName)
            views.setTextColor(R.id.month_name, primaryTextColor)
        } catch (ignored: Exception) {
        }

        try {
            views.setTextViewText(R.id.month_year_text, fullMonth)
            views.setTextColor(R.id.month_year_text, primaryTextColor)
        } catch (ignored: Exception) {
        }

        try {
            views.setTextViewText(R.id.current_day_number, dayNumber)
            views.setTextColor(R.id.current_day_number, primaryTextColor)
        } catch (ignored: Exception) {
        }
        try {
            val cDayNumBitmap = createTextBitmap(context, dayNumber, 320f, primaryTextColor)
            views.setImageViewBitmap(R.id.current_day_number, cDayNumBitmap)
        } catch (ignored: Exception) {
        }

        try {
            views.setTextViewText(R.id.current_day_name, dayOfWeek)
            views.setTextColor(R.id.current_day_name, accentColor)
        } catch (ignored: Exception) {
        }
        try {
            val cDayNameBitmap = createTextBitmap(context, dayOfWeek, 110f, accentColor)
            views.setImageViewBitmap(R.id.current_day_name, cDayNameBitmap)
        } catch (ignored: Exception) {
        }

        try {
            views.setTextViewText(R.id.current_month_name, monthName)
            views.setTextColor(R.id.current_month_name, primaryTextColor)
        } catch (ignored: Exception) {
        }
        try {
            val cMonthBitmap = createTextBitmap(context, monthName, 110f, primaryTextColor)
            views.setImageViewBitmap(R.id.current_month_name, cMonthBitmap)
        } catch (ignored: Exception) {
        }

        // 3. calendar_content_image (Calendar13, 14, 15)
        try {
            val combinedBitmap = renderMonthGridBitmap(context, calendar, primaryTextColor, accentColor)
            views.setImageViewBitmap(R.id.calendar_content_image, combinedBitmap)
        } catch (ignored: Exception) {
        }

        // 4. Month Grid TextViews: header_day_0..6 and day_1..42 (Calendar10, Calendar11, etc.)
        val todayDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...

        val headerIds = intArrayOf(
            R.id.header_day_0, R.id.header_day_1, R.id.header_day_2,
            R.id.header_day_3, R.id.header_day_4, R.id.header_day_5, R.id.header_day_6
        )
        for (i in 0..6) {
            val isTodayHeader = (todayDayOfWeek - 1) == i
            val hColor = if (isTodayHeader) accentColor else primaryTextColor
            try {
                views.setTextColor(headerIds[i], hColor)
            } catch (_: Exception) {}
        }

        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1 (Sun) to 7 (Sat)
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val startOffset = firstDayOfWeek
        var hasRow6 = false

        for (cellIndex in 1..42) {
            val dayIdName = "day_$cellIndex"
            val resId = context.resources.getIdentifier(dayIdName, "id", context.packageName)
            if (resId != 0) {
                val dNum = cellIndex - startOffset + 1
                if (dNum in 1..daysInMonth) {
                    views.setTextViewText(resId, dNum.toString())
                    if (dNum == todayDayOfMonth) {
                        views.setTextColor(resId, accentColor)
                    } else {
                        views.setTextColor(resId, primaryTextColor)
                    }
                    if (cellIndex > 35) {
                        hasRow6 = true
                    }
                } else {
                    views.setTextViewText(resId, "")
                }
            }
        }
        try {
            views.setViewVisibility(R.id.last_row, if (hasRow6) android.view.View.VISIBLE else android.view.View.GONE)
        } catch (_: Exception) {}
    }

    private fun renderMonthGridBitmap(
        context: Context,
        calendar: Calendar,
        textColor: Int,
        accentColor: Int
    ): Bitmap {
        val width = 600
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = getFont(context)
            textSize = 36f
            color = accentColor
            textAlign = Paint.Align.CENTER
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = getFont(context)
            textSize = 28f
            color = textColor
            textAlign = Paint.Align.CENTER
        }

        val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }

        val todayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = getFont(context)
            textSize = 28f
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }

        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        canvas.drawText(monthYear, width / 2f, 50f, headerPaint)

        val daysOfWeek = arrayOf("S", "M", "T", "W", "T", "F", "S")
        val colWidth = width / 7f
        for (i in daysOfWeek.indices) {
            canvas.drawText(daysOfWeek[i], colWidth * i + colWidth / 2f, 105f, headerPaint)
        }

        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayCol = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = calendar.get(Calendar.DAY_OF_MONTH)

        var row = 0
        var col = firstDayCol
        val rowHeight = 45f
        val startY = 160f

        for (d in 1..maxDays) {
            val cx = colWidth * col + colWidth / 2f
            val cy = startY + row * rowHeight

            if (d == today) {
                canvas.drawCircle(cx, cy - 10f, 22f, todayPaint)
                canvas.drawText(d.toString(), cx, cy, todayTextPaint)
            } else {
                canvas.drawText(d.toString(), cx, cy, textPaint)
            }

            col++
            if (col > 6) {
                col = 0
                row++
            }
        }

        return bitmap
    }

    private fun scheduleMidnightUpdate(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 5)
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
            Log.w(TAG, "Failed to schedule midnight update: ${e.message}")
        }
    }
}
