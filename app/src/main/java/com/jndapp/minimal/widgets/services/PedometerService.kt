package com.jndapp.minimal.widgets.services

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jndapp.minimal.widgets.ApplicationClass
import com.jndapp.minimal.widgets.R

class PedometerService : Service(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values.firstOrNull()?.toInt() ?: return
            val prefs = getSharedPreferences("PedometerPrefs", Context.MODE_PRIVATE)
            val initialSteps = prefs.getInt("InitialStepCount", -1)

            val currentTodaySteps = if (initialSteps == -1) {
                prefs.edit().putInt("InitialStepCount", totalSteps).apply()
                0
            } else {
                (totalSteps - initialSteps).coerceAtLeast(0)
            }

            prefs.edit().putInt("CurrentSteps", currentTodaySteps).apply()

            val updateIntent = Intent("com.jndapp.minimal.widgets.ACTION_STEP_UPDATE")
            sendBroadcast(updateIntent)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, ApplicationClass.CHANNEL_PEDOMETER)
            .setContentTitle("Step Counter Active")
            .setContentText("Minimal step tracking active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1002
    }
}
