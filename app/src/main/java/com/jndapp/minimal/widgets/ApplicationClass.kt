package com.jndapp.minimal.widgets

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.let { manager ->
                val keepAliveChannel = NotificationChannel(
                    CHANNEL_KEEP_ALIVE,
                    "Widget Keep Alive Service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Keeps widgets synchronized and updated in background"
                    setShowBadge(false)
                    enableVibration(false)
                    enableLights(false)
                    setSound(null, null)
                }

                val compassChannel = NotificationChannel(
                    CHANNEL_COMPASS,
                    "Compass Service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Background service for compass widget orientation updates"
                    setShowBadge(false)
                }

                val pedometerChannel = NotificationChannel(
                    CHANNEL_PEDOMETER,
                    "Pedometer Service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Foreground service tracking steps for pedometer widgets"
                    setShowBadge(false)
                }

                manager.createNotificationChannel(keepAliveChannel)
                manager.createNotificationChannel(compassChannel)
                manager.createNotificationChannel(pedometerChannel)
            }
        }
    }

    companion object {
        const val CHANNEL_KEEP_ALIVE = "widget_keep_alive_channel"
        const val CHANNEL_COMPASS = "compass_service_channel"
        const val CHANNEL_PEDOMETER = "pedometer_service_channel"

        lateinit var instance: ApplicationClass
            private set
    }
}
