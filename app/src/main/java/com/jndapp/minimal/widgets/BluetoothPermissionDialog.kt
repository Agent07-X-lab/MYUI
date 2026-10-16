package com.jndapp.minimal.widgets

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class BluetoothPermissionDialog : ComponentActivity() {

    private val requestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasPerm = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPerm) {
                onPermissionGranted()
                finish()
                return
            }

            AlertDialog.Builder(this)
                .setTitle("Bluetooth Permission Required")
                .setMessage("Allow access to Bluetooth devices to show connection status and battery levels on widgets.")
                .setPositiveButton("Grant Permission") { _, _ ->
                    requestLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                }
                .setNegativeButton("Cancel") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        } else {
            onPermissionGranted()
            finish()
        }
    }

    private fun onPermissionGranted() {
        sendBroadcast(Intent("com.jndapp.minimal.widgets.ACTION_UPDATE_BUDS_STATUS"))
        sendBroadcast(Intent("com.jndapp.minimal.widgets.PERMISSION_GRANTED"))
        Toast.makeText(this, "Bluetooth widgets updated", Toast.LENGTH_SHORT).show()
    }
}
