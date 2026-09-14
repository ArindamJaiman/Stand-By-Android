package com.standbypro.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.standbypro.domain.PhysicalOrientation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import android.util.Log
import kotlin.math.abs

class OrientationMonitor(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // Request wake-up accelerometer if hardware supports it, with standard fallback
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER, true)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val orientation: Flow<PhysicalOrientation> = callbackFlow {
        var currentOrientation = PhysicalOrientation.UNKNOWN

        // Immediately send initial value so combine downstream does not block
        trySend(currentOrientation)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    // If phone is flat on a desk (z ≈ 9.8, x & y small), it's not mounted on a stand
                    val isFlat = abs(z) > 8.8f && abs(x) < 2.5f && abs(y) < 2.5f

                    val newOrientation = if (isFlat) {
                        PhysicalOrientation.UNKNOWN
                    } else when {
                        // Sideways gravity dominates -> Landscape
                        x > 3.5f && abs(x) > abs(y) * 1.15f -> PhysicalOrientation.LANDSCAPE_LEFT
                        x < -3.5f && abs(x) > abs(y) * 1.15f -> PhysicalOrientation.LANDSCAPE_RIGHT
                        // Vertical gravity dominates (upright or inverted) -> Portrait
                        abs(y) > 3.5f && abs(y) > abs(x) * 1.15f -> PhysicalOrientation.PORTRAIT
                        else -> currentOrientation
                    }

                    if (newOrientation != currentOrientation) {
                        currentOrientation = newOrientation
                        trySend(currentOrientation)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accelerometer != null) {
            Log.i("OrientationMonitor", "Registering accelerometer listener (Charging active)")
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Log.w("OrientationMonitor", "No accelerometer sensor found on device")
            trySend(PhysicalOrientation.UNKNOWN)
        }

        awaitClose {
            Log.i("OrientationMonitor", "Unregistering accelerometer listener (Entering hibernation)")
            sensorManager.unregisterListener(listener)
        }
    }.distinctUntilChanged()
}

