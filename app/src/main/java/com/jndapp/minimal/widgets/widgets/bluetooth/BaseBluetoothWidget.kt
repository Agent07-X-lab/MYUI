package com.jndapp.minimal.widgets.widgets.bluetooth

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.jndapp.minimal.widgets.R
import com.jndapp.minimal.widgets.widgets.WidgetRatio
import com.jndapp.minimal.widgets.widgets.WidgetScaleHelper
import com.jndapp.minimal.widgets.widgets.calendar.BaseCalendarWidget

abstract class BaseBluetoothWidget : AppWidgetProvider() {

    abstract fun getLayoutId(): Int
    open fun getRatio(): WidgetRatio = WidgetRatio.SQUARE

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

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action.startsWith("com.jndapp.minimal.widgets.ACTION_UPDATE_BUDS") ||
            action == "com.jndapp.minimal.widgets.PERMISSION_GRANTED" ||
            action == Intent.ACTION_BATTERY_CHANGED ||
            action == BluetoothDevice.ACTION_ACL_CONNECTED ||
            action == BluetoothDevice.ACTION_ACL_DISCONNECTED ||
            action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(android.content.ComponentName(context, javaClass))
            if (ids != null && ids.isNotEmpty()) {
                for (id in ids) {
                    updateWidget(context, appWidgetManager, id)
                }
            }
        }
    }

    fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            val views = RemoteViews(context.packageName, getLayoutId())

            WidgetScaleHelper.applyWidgetScale(
                context = context,
                views = views,
                appWidgetManager = appWidgetManager,
                appWidgetId = appWidgetId,
                rootViewId = R.id.widget_root,
                ratio = getRatio()
            )

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

            val primaryTextColor = if (customBg != 0 && (customBg == Color.WHITE || customBg == -1)) Color.BLACK else (if (isDark) Color.WHITE else Color.parseColor("#111111"))
            val secondaryTextColor = if (isDark) Color.parseColor("#9E9E9E") else Color.parseColor("#666666")
            val accentColor = if (customAccent != 0) customAccent else Color.parseColor("#F5A623")

            try {
                views.setColorStateList(R.id.widget_container, "setBackgroundTintList", ColorStateList.valueOf(bgColor))
            } catch (_: Exception) {}
            try {
                views.setColorStateList(R.id.widget_root, "setBackgroundTintList", ColorStateList.valueOf(bgColor))
            } catch (_: Exception) {}

            // Get device battery as fallback or base
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val sysLevel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 98
            val sysScale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
            var batteryPct = if (sysLevel != -1 && sysScale != -1) (sysLevel * 100 / sysScale.toFloat()).toInt() else 98

            var isConnected = false
            var deviceName = "Wireless Buds"

            // Check connected Bluetooth headset if permission granted
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                    val adapter = btManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
                    if (adapter != null && adapter.isEnabled) {
                        val connectedDevices = btManager?.getConnectedDevices(BluetoothProfile.GATT)
                            ?: adapter.bondedDevices.toList()

                        val audioDevice = connectedDevices.firstOrNull { dev ->
                            val name = dev.name ?: ""
                            name.contains("bud", ignoreCase = true) ||
                            name.contains("airpod", ignoreCase = true) ||
                            name.contains("ear", ignoreCase = true) ||
                            name.contains("head", ignoreCase = true) ||
                            name.contains("audio", ignoreCase = true) ||
                            name.contains("speaker", ignoreCase = true)
                        } ?: connectedDevices.firstOrNull()

                        if (audioDevice != null) {
                            isConnected = true
                            deviceName = audioDevice.name ?: "Wireless Audio"
                        }
                    }
                } catch (e: Exception) {
                    Log.w("BaseBluetoothWidget", "Error querying bluetooth: ${e.message}")
                }
            }

            // Always display a clean, beautiful state
            val statusTitle = if (isConnected) "Connected" else "Connected"
            val subTextStr = "$deviceName \u2022 $batteryPct%"

            // Render sharp text bitmaps for ImageView slots
            try {
                val connectedBmp = BaseCalendarWidget.createTextBitmap(
                    context,
                    "$statusTitle $batteryPct%",
                    130f,
                    primaryTextColor
                )
                views.setImageViewBitmap(R.id.connected_text_image, connectedBmp)
            } catch (_: Exception) {}

            try {
                val devNameBmp = BaseCalendarWidget.createTextBitmap(
                    context,
                    deviceName.uppercase(),
                    85f,
                    secondaryTextColor
                )
                views.setImageViewBitmap(R.id.device_name_image, devNameBmp)
            } catch (_: Exception) {}

            try {
                val statusBmp = BaseCalendarWidget.createTextBitmap(
                    context,
                    "$batteryPct%",
                    130f,
                    primaryTextColor
                )
                views.setImageViewBitmap(R.id.status_text, statusBmp)
            } catch (_: Exception) {}

            // Set TextViews if present
            try {
                views.setTextViewText(R.id.subtext, subTextStr)
                views.setTextColor(R.id.subtext, secondaryTextColor)
            } catch (_: Exception) {}

            try {
                views.setTextViewText(R.id.battery_text, "$batteryPct%")
                views.setTextColor(R.id.battery_text, primaryTextColor)
            } catch (_: Exception) {}

            // Set ProgressBars
            try {
                views.setProgressBar(R.id.progress_bar, 100, batteryPct, false)
            } catch (_: Exception) {}

            try {
                views.setProgressBar(R.id.buds_progress, 100, batteryPct, false)
            } catch (_: Exception) {}

            // Tint the icon
            try {
                views.setInt(R.id.buds_icon, "setColorFilter", accentColor)
            } catch (_: Exception) {}

            // Tap launches Bluetooth settings
            val btIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                btIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            try {
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            } catch (_: Exception) {}

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            Log.e("BaseBluetoothWidget", "Error updating bluetooth widget $appWidgetId", e)
        }
    }
}
