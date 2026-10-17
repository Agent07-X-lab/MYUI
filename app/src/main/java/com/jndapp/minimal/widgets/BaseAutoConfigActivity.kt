package com.jndapp.minimal.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle

open class BaseAutoConfigActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)

            val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                `package` = packageName
            }
            sendBroadcast(updateIntent)
        } else {
            setResult(RESULT_CANCELED)
        }

        finish()
    }
}

class AppFolderWidgetConfigActivity : BaseAutoConfigActivity()
class TextWidgetConfigActivity : BaseAutoConfigActivity()
class PedometerConfigActivity : BaseAutoConfigActivity()
class QuickNoteEditActivity7 : BaseAutoConfigActivity()
