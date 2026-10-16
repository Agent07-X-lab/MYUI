package com.jndapp.minimal.widgets.services

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jndapp.minimal.widgets.ApplicationClass
import com.jndapp.minimal.widgets.R

class WidgetKeepAliveService : Service() {

    override fun onCreate() {
        super.onCreate()
        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, ApplicationClass.CHANNEL_KEEP_ALIVE)
            .setContentTitle("Minimal Widgets")
            .setContentText("Keeping widgets synchronized")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
