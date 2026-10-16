package com.jndapp.minimal.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.util.Log

class NotificationListener : NotificationListenerService() {

    private var mediaSessionManager: MediaSessionManager? = null

    private val callback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            broadcastMediaUpdate()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            broadcastMediaUpdate()
        }
    }

    override fun onCreate() {
        super.onCreate()
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            val sessions = mediaSessionManager?.getActiveSessions(
                ComponentName(this, NotificationListener::class.java)
            )
            sessions?.forEach { controller ->
                controller.registerCallback(callback)
            }
            broadcastMediaUpdate()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException accessing media sessions", e)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            val sessions = mediaSessionManager?.getActiveSessions(
                ComponentName(this, NotificationListener::class.java)
            )
            sessions?.forEach { controller ->
                controller.unregisterCallback(callback)
            }
        } catch (_: Exception) {}
    }

    private fun broadcastMediaUpdate() {
        val intent = Intent("com.jndapp.minimal.widgets.ACTION_MEDIA_UPDATE")
        sendBroadcast(intent)
    }

    companion object {
        private const val TAG = "NotificationListener"
    }
}
