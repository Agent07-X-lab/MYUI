package com.jndapp.minimal.widgets.billing

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.jndapp.minimal.widgets.QuickNoteEditActivity
import com.jndapp.minimal.widgets.widgets.weather.WeatherWidgetConfigActivity

/**
 * Open-Source Widget Auto-Configurator
 *
 * In commercial builds, this activity served as a paywall gate requiring in-app purchases.
 * In this Free & Open Source edition, all pro subscriptions and paywalls are completely cut:
 * every widget is 100% unlocked, immediately placed, and rendered with zero purchase or license checks.
 */
class PremiumGateConfigActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        Log.d("PremiumGateConfig", "Widget configure requested for appWidgetId: $appWidgetId")

        // Default result is CANCELED if user backs out
        setResult(RESULT_CANCELED)

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
            val providerName = info?.provider?.className ?: ""

            // Check if this widget requires a dedicated setup screen
            when {
                providerName.contains("weather", ignoreCase = true) -> {
                    try {
                        val configIntent = Intent(this, WeatherWidgetConfigActivity::class.java).apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        startActivityForResult(configIntent, REQUEST_SUB_CONFIG)
                        return
                    } catch (e: Exception) {
                        Log.w("PremiumGateConfig", "Could not start weather config: ${e.message}")
                    }
                }
                providerName.contains("note", ignoreCase = true) || providerName.contains("remind", ignoreCase = true) -> {
                    try {
                        val configIntent = Intent(this, QuickNoteEditActivity::class.java).apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        startActivityForResult(configIntent, REQUEST_SUB_CONFIG)
                        return
                    } catch (e: Exception) {
                        Log.w("PremiumGateConfig", "Could not start note config: ${e.message}")
                    }
                }
            }

            // For all other widgets: all features unlocked, place widget immediately
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)

            // Trigger update broadcast so widget views render right away
            if (info != null) {
                val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    component = info.provider
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                }
                sendBroadcast(updateIntent)
            }
        }

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SUB_CONFIG) {
            setResult(resultCode, data)
        }
        finish()
    }

    companion object {
        private const val REQUEST_SUB_CONFIG = 1001
    }
}
