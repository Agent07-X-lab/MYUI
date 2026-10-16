package com.jndapp.minimal.widgets

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

class AutorotatePermissionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Settings.System.canWrite(this)) {
            sendBroadcast(Intent("com.jndapp.minimal.widgets.ACTION_PERMISSION_GRANTED_AUTOROTATE"))
        }
        finish()
    }
}
