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
import kotlin.math.abs
import kotlin.math.atan2

class OrientationMonitor(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val orientation: Flow<PhysicalOrientation> = callbackFlow {
        var currentOrientation = PhysicalOrientation.UNKNOWN
        
        // Hysteresis variables
        val LANDSCAPE_THRESHOLD = 5.0f // m/s^2 on the X axis
        val PORTRAIT_THRESHOLD = 5.0f // m/s^2 on the Y axis
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    
                    val newOrientation = when {
                        x > LANDSCAPE_THRESHOLD && abs(y) < PORTRAIT_THRESHOLD -> PhysicalOrientation.LANDSCAPE_LEFT
                        x < -LANDSCAPE_THRESHOLD && abs(y) < PORTRAIT_THRESHOLD -> PhysicalOrientation.LANDSCAPE_RIGHT
                        y > PORTRAIT_THRESHOLD && abs(x) < LANDSCAPE_THRESHOLD -> PhysicalOrientation.PORTRAIT
                        else -> currentOrientation // Keep current if in a transitional or flat state
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
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            // Fallback for devices without accelerometer if needed
            trySend(PhysicalOrientation.UNKNOWN)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.distinctUntilChanged()
}
