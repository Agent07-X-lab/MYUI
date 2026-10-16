package com.jndapp.minimal.widgets

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog

class NotificationAccessDialog : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AlertDialog.Builder(this)
            .setTitle("Notification Access Required")
            .setMessage("Allow MyUI notification access to display currently playing music track title and artist.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                finish()
            }
            .setNegativeButton("Not Now") { _, _ -> finish() }
            .setOnDismissListener { finish() }
            .show()
    }
}
