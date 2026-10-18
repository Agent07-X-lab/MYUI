package com.jndapp.minimal.widgets.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.MainActivity
import com.jndapp.minimal.widgets.R
import android.provider.ContactsContract
import android.provider.MediaStore
import com.jndapp.minimal.widgets.widgets.device.DeviceMetricsRenderer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

open class UniversalWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "UniversalWidgetProvider"
    }

    open fun getCustomLayoutId(): Int = 0
    open fun getCustomRatio(): WidgetRatio = WidgetRatio.SQUARE

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

    open fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
            val layoutId = if (getCustomLayoutId() != 0) {
                getCustomLayoutId()
            } else {
                info?.initialLayout ?: 0
            }

            if (layoutId == 0) {
                Log.w(TAG, "No initial layout found for widget $appWidgetId")
                return
            }

            val views = RemoteViews(context.packageName, layoutId)

            val dimensions = WidgetScaleHelper.applyWidgetScale(
                context = context,
                views = views,
                appWidgetManager = appWidgetManager,
                appWidgetId = appWidgetId,
                rootViewId = R.id.widget_root,
                ratio = getCustomRatio()
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

            val studioPrefs = context.getSharedPreferences("studio_prefs", Context.MODE_PRIVATE)
            val widgetPrefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)

            val customBg = studioPrefs.getInt("theme_background_color", 0)
            val customAccent = studioPrefs.getInt("theme_accent_color", 0)
            val opacity = widgetPrefs.getFloat("widget_opacity", 1.0f).coerceIn(0.1f, 1.0f)

            val baseBgColor = if (customBg != 0) customBg else (if (isDark) Color.parseColor("#1E1E1E") else Color.parseColor("#F5F5F7"))
            val alpha = (opacity * 255).toInt()
            val bgColor = Color.argb(alpha, Color.red(baseBgColor), Color.green(baseBgColor), Color.blue(baseBgColor))

            val primaryTextColor = if (customAccent != 0) customAccent else (if (isDark) Color.WHITE else Color.parseColor("#111111"))
            val secondaryTextColor = if (isDark) Color.parseColor("#B0B0B0") else Color.parseColor("#666666")

            try {
                views.setColorStateList(R.id.widget_container, "setBackgroundTintList", ColorStateList.valueOf(bgColor))
            } catch (ignored: Exception) {
            }
            try {
                views.setColorStateList(R.id.widget_root, "setBackgroundTintList", ColorStateList.valueOf(bgColor))
            } catch (ignored: Exception) {
            }

            // Populate time / date if text views exist
            val calendar = Calendar.getInstance()
            val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
            val dayText = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time).uppercase()
            val dateText = SimpleDateFormat("d MMM", Locale.getDefault()).format(calendar.time)

            try {
                views.setTextViewText(R.id.time_display, timeText)
                views.setTextColor(R.id.time_display, primaryTextColor)
            } catch (ignored: Exception) {
            }

            try {
                views.setTextViewText(R.id.date_day_display, "$dayText, $dateText")
                views.setTextColor(R.id.date_day_display, secondaryTextColor)
            } catch (ignored: Exception) {
            }

            val thisClassName = javaClass.name
            val isCameraWidget = thisClassName.contains("camera", ignoreCase = true)
            val isContactWidget = thisClassName.contains("contact", ignoreCase = true)
            val isDeviceWidget = thisClassName.contains("device", ignoreCase = true) ||
                    thisClassName.contains("dots", ignoreCase = true) ||
                    layoutId == R.layout.widget_other_layout ||
                    layoutId == R.layout.dots_ram_layout ||
                    layoutId == R.layout.dots_storage_layout ||
                    layoutId == R.layout.dots_temp_layout ||
                    layoutId == R.layout.dots_volume_layout

            // 1. Device metrics rendering for all 22 device widgets
            if (isDeviceWidget) {
                try {
                    val metricBitmap = DeviceMetricsRenderer.renderDeviceMetricBitmap(
                        context = context,
                        widgetClassName = thisClassName,
                        accentColor = primaryTextColor,
                        primaryTextColor = if (isDark) Color.WHITE else Color.parseColor("#111111")
                    )
                    views.setImageViewBitmap(R.id.iv_widget_other, metricBitmap)
                    views.setImageViewBitmap(R.id.content_image, metricBitmap)
                } catch (e: Exception) {
                    Log.w(TAG, "Error rendering device metric for $thisClassName", e)
                }
            }

            // 2. Contact widgets binding
            if (isContactWidget) {
                try {
                    views.setTextViewText(R.id.contact_name, "Favorite Contact")
                    views.setTextColor(R.id.contact_name, primaryTextColor)
                    views.setImageViewResource(R.id.contact_image, R.drawable.default_contact_image)

                    for (i in 1..8) {
                        val nameRes = context.resources.getIdentifier("contact_name_$i", "id", context.packageName)
                        val imgRes = context.resources.getIdentifier("contact_image_$i", "id", context.packageName)
                        if (nameRes != 0) {
                            views.setTextViewText(nameRes, "Contact $i")
                            views.setTextColor(nameRes, primaryTextColor)
                        }
                        if (imgRes != 0) {
                            views.setImageViewResource(imgRes, R.drawable.default_contact_image)
                        }
                    }

                    // Call button intent
                    val callIntent = Intent(Intent.ACTION_DIAL).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    val callPending = PendingIntent.getActivity(
                        context,
                        appWidgetId * 10 + 1,
                        callIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.call_button, callPending)

                    // Message button intent
                    val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    val smsPending = PendingIntent.getActivity(
                        context,
                        appWidgetId * 10 + 2,
                        smsIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.message_button, smsPending)

                    // Contacts app intent
                    val contactsIntent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    val contactsPending = PendingIntent.getActivity(
                        context,
                        appWidgetId * 10 + 3,
                        contactsIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.contact_container, contactsPending)
                    views.setOnClickPendingIntent(R.id.widget_container, contactsPending)
                    views.setOnClickPendingIntent(R.id.widget_root, contactsPending)
                } catch (e: Exception) {
                    Log.w(TAG, "Error setting contact views: ${e.message}")
                }
            }

            // 3. Click intent configuration
            val clickIntent = when {
                isCameraWidget -> {
                    Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                isContactWidget -> {
                    Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                else -> {
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            } catch (ignored: Exception) {}

            try {
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            } catch (ignored: Exception) {}

            try {
                views.setOnClickPendingIntent(R.id.camera_icon, pendingIntent)
            } catch (ignored: Exception) {}

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating universal widget $appWidgetId", e)
        }
    }
}

class TestWidget : UniversalWidgetProvider()
class WidgetPinnedReceiver : UniversalWidgetProvider()
