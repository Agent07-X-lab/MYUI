package com.jndapp.minimal.widgets.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BootReceiver", "Device boot received, services initialized")
    }
}

class WidgetClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WidgetClickReceiver", "Widget click received: ${intent.action}")
    }
}

class WidgetFallbackReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WidgetFallbackReceiver", "Fallback received: ${intent.action}")
    }
}
