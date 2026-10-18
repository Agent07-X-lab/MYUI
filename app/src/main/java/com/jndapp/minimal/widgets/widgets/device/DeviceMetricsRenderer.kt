package com.jndapp.minimal.widgets.widgets.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.view.WindowManager
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*

object DeviceMetricsRenderer {

    fun renderDeviceMetricBitmap(
        context: Context,
        widgetClassName: String,
        accentColor: Int,
        primaryTextColor: Int,
        widthPx: Int = 440,
        heightPx: Int = 220
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val simpleName = widgetClassName.substringAfterLast('.')

        when (simpleName) {
            "DotsRam" -> return renderDotsGauge(context, getRamPercent(context), "RAM", accentColor, widthPx, heightPx)
            "DotsStorage" -> return renderDotsGauge(context, getStoragePercent(), "STORAGE", accentColor, widthPx, heightPx)
            "DotsTemp" -> return renderDotsGauge(context, getBatteryTempPercent(context), "TEMP", accentColor, widthPx, heightPx)
            "DotsVolume" -> return renderDotsGauge(context, getVolumePercent(context), "VOLUME", accentColor, widthPx, heightPx)
        }

        val (title, mainValue, subValue, progressPct) = getDeviceMetricData(context, simpleName)

        drawCardMetric(canvas, widthPx, heightPx, title, mainValue, subValue, progressPct, accentColor, primaryTextColor)
        return bitmap
    }

    private data class MetricData(
        val title: String,
        val mainValue: String,
        val subValue: String,
        val progressPercent: Int = -1
    )

    private fun getDeviceMetricData(context: Context, simpleName: String): MetricData {
        return when (simpleName) {
            "DeviceRAM" -> {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                val memInfo = ActivityManager.MemoryInfo()
                am?.getMemoryInfo(memInfo)
                val totalGb = (memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)).coerceAtLeast(0.1)
                val availGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)
                val usedGb = (totalGb - availGb).coerceAtLeast(0.0)
                val pct = ((usedGb / totalGb) * 100).toInt().coerceIn(0, 100)
                MetricData("RAM USAGE", String.format(Locale.US, "%.1f / %.1f GB", usedGb, totalGb), "$pct% USED", pct)
            }
            "DeviceStorage" -> {
                val stat = StatFs(Environment.getDataDirectory().path)
                val totalGb = (stat.blockCountLong * stat.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)
                val freeGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)
                val usedGb = (totalGb - freeGb).coerceAtLeast(0.0)
                val pct = if (totalGb > 0) ((usedGb / totalGb) * 100).toInt() else 0
                MetricData("STORAGE", String.format(Locale.US, "%.1f / %.0f GB", usedGb, totalGb), "$pct% USED", pct)
            }
            "DeviceStorageRemain" -> {
                val stat = StatFs(Environment.getDataDirectory().path)
                val totalGb = (stat.blockCountLong * stat.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)
                val freeGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)
                val pct = if (totalGb > 0) ((freeGb / totalGb) * 100).toInt() else 0
                MetricData("FREE STORAGE", String.format(Locale.US, "%.1f GB FREE", freeGb), "$pct% AVAILABLE", pct)
            }
            "DeviceCPU" -> {
                val cores = Runtime.getRuntime().availableProcessors()
                MetricData("PROCESSOR", "$cores-CORE CPU", Build.HARDWARE.uppercase(), 100)
            }
            "DeviceBatteryTemp" -> {
                val bStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val tempRaw = bStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300) ?: 300
                val tempC = tempRaw / 10.0
                val tempF = tempC * 9 / 5 + 32
                MetricData("BATTERY TEMP", String.format(Locale.US, "%.1f°C", tempC), String.format(Locale.US, "%.1f°F", tempF), (tempC * 2).toInt().coerceIn(0, 100))
            }
            "DeviceBatteryVoltage" -> {
                val bStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val voltRaw = bStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000) ?: 4000
                val volt = voltRaw / 1000.0
                MetricData("BATTERY VOLTAGE", String.format(Locale.US, "%.2f V", volt), "NORMAL RANGE", 80)
            }
            "DeviceChargeStatus" -> {
                val bStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val status = bStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                val level = bStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, 50) ?: 50
                MetricData("CHARGE STATUS", if (isCharging) "CHARGING" else "DISCHARGING", "$level% BATTERY", level)
            }
            "DeviceTimeToCharge" -> {
                val bStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val status = bStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                val level = bStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, 50) ?: 50
                MetricData("TIME TO FULL", if (isCharging) "Fast Charging" else "On Battery", "$level% Current Level", level)
            }
            "DeviceUptime" -> {
                val uptimeSec = SystemClock.elapsedRealtime() / 1000
                val days = uptimeSec / (24 * 3600)
                val hours = (uptimeSec % (24 * 3600)) / 3600
                val mins = (uptimeSec % 3600) / 60
                val uptimeStr = if (days > 0) "${days}d ${hours}h ${mins}m" else "${hours}h ${mins}m"
                MetricData("SYSTEM UPTIME", uptimeStr, "Since Last Boot", -1)
            }
            "DeviceLastBoot" -> {
                val bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
                val bootStr = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(bootTime))
                MetricData("LAST BOOT", bootStr, "System Restart", -1)
            }
            "DeviceName" -> {
                MetricData("DEVICE", Build.MODEL, "${Build.MANUFACTURER.uppercase()} ${Build.DEVICE}", -1)
            }
            "DeviceOS" -> {
                MetricData("OPERATING SYSTEM", "Android ${Build.VERSION.RELEASE}", "API ${Build.VERSION.SDK_INT}", -1)
            }
            "DeviceResolution" -> {
                val dm = context.resources.displayMetrics
                MetricData("DISPLAY", "${dm.widthPixels} × ${dm.heightPixels}", "${dm.densityDpi} DPI", -1)
            }
            "DeviceRefreshRate" -> {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                val rate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    context.display?.refreshRate ?: 60f
                } else {
                    @Suppress("DEPRECATION")
                    wm?.defaultDisplay?.refreshRate ?: 60f
                }
                MetricData("REFRESH RATE", "${rate.toInt()} Hz", "High Fluidity Display", 90)
            }
            "DeviceIP" -> {
                val ip = getLocalIpAddress() ?: "127.0.0.1"
                MetricData("IP ADDRESS", ip, "Local Network", -1)
            }
            "DeviceNetwork" -> {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val activeNetwork = cm?.activeNetwork
                val caps = cm?.getNetworkCapabilities(activeNetwork)
                val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
                val netStr = when {
                    isWifi -> "Wi-Fi Connected"
                    isCellular -> "Mobile Network"
                    else -> "Network Active"
                }
                MetricData("NETWORK", netStr, "Internet Available", -1)
            }
            "DeviceDataUsage" -> {
                val totalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()
                val totalGb = if (totalBytes > 0) totalBytes / (1024.0 * 1024.0 * 1024.0) else 0.0
                MetricData("DATA USAGE", String.format(Locale.US, "%.2f GB", totalGb), "Total Since Boot", -1)
            }
            "DeviceSpeedTest" -> {
                val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                @Suppress("DEPRECATION")
                val linkSpeed = wm?.connectionInfo?.linkSpeed ?: 150
                MetricData("LINK SPEED", "$linkSpeed Mbps", "Wi-Fi Connection", (linkSpeed / 8).coerceIn(0, 100))
            }
            else -> {
                MetricData("SYSTEM", Build.MODEL, "Ready", -1)
            }
        }
    }

    private fun drawCardMetric(
        canvas: Canvas,
        w: Int,
        h: Int,
        title: String,
        mainValue: String,
        subValue: String,
        progressPct: Int,
        accentColor: Int,
        textColor: Int
    ) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = h * 0.12f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }
        canvas.drawText(title.uppercase(), 28f, h * 0.28f, titlePaint)

        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = (h * 0.26f).coerceAtMost(w * 0.11f)
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(mainValue, 28f, h * 0.62f, valuePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(190, Color.red(textColor), Color.green(textColor), Color.blue(textColor))
            textSize = h * 0.13f
            typeface = Typeface.DEFAULT
        }
        canvas.drawText(subValue, 28f, h * 0.86f, subPaint)

        if (progressPct in 0..100) {
            val barW = w * 0.32f
            val barH = h * 0.06f
            val barX = w - barW - 28f
            val barY = h * 0.78f
            val barRect = RectF(barX, barY, barX + barW, barY + barH)

            val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(40, 255, 255, 255)
            }
            canvas.drawRoundRect(barRect, barH / 2f, barH / 2f, trackPaint)

            val fillW = barW * (progressPct / 100f)
            if (fillW > 0) {
                val fillRect = RectF(barX, barY, barX + fillW, barY + barH)
                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                }
                canvas.drawRoundRect(fillRect, barH / 2f, barH / 2f, fillPaint)
            }
        }
    }

    private fun renderDotsGauge(
        context: Context,
        percent: Int,
        title: String,
        accentColor: Int,
        w: Int,
        h: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = h * 0.14f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }
        canvas.drawText("$title • $percent%", 24f, h * 0.26f, titlePaint)

        // Draw 4 rows of 12 dots
        val rows = 4
        val cols = 12
        val totalDots = rows * cols
        val activeDots = ((percent / 100f) * totalDots).toInt().coerceIn(0, totalDots)

        val startX = 24f
        val startY = h * 0.42f
        val dotRadius = (h * 0.06f).coerceAtMost(10f)
        val stepX = (w - 48f) / cols
        val stepY = (h * 0.48f) / rows

        val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
        }
        val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, 255, 255, 255)
        }

        var dotIndex = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cx = startX + (c * stepX) + (stepX / 2f)
                val cy = startY + (r * stepY) + (stepY / 2f)
                val paint = if (dotIndex < activeDots) activePaint else inactivePaint
                canvas.drawCircle(cx, cy, dotRadius, paint)
                dotIndex++
            }
        }

        return bitmap
    }

    private fun getRamPercent(context: Context): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 50
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val used = memInfo.totalMem - memInfo.availMem
        return ((used.toDouble() / memInfo.totalMem) * 100).toInt().coerceIn(0, 100)
    }

    private fun getStoragePercent(): Int {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        val used = total - free
        return if (total > 0) ((used.toDouble() / total) * 100).toInt().coerceIn(0, 100) else 50
    }

    private fun getBatteryTempPercent(context: Context): Int {
        val bStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val tempRaw = bStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300) ?: 300
        return ((tempRaw / 10.0) * 2).toInt().coerceIn(0, 100)
    }

    private fun getVolumePercent(context: Context): Int {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return 50
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        return ((current * 100) / max).coerceIn(0, 100)
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val host = addr.hostAddress ?: continue
                        if (host.indexOf(':') < 0) return host
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }
}
