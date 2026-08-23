package com.standbypro.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class AmbientLightMonitor(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    val isNightMode: Flow<Boolean> = callbackFlow {
        var currentState = false
        
        // Hysteresis thresholds for light
        val NIGHT_THRESHOLD_LUX = 5.0f
        val DAY_THRESHOLD_LUX = 15.0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor.type != Sensor.TYPE_LIGHT) return
                
                val lux = event.values[0]
                
                val newState = when {
                    lux < NIGHT_THRESHOLD_LUX -> true
                    lux > DAY_THRESHOLD_LUX -> false
                    else -> currentState
                }

                if (newState != currentState) {
                    currentState = newState
                    trySend(currentState)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (lightSensor != null) {
            sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            // Fallback if no light sensor is present
            trySend(false)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.distinctUntilChanged()
}
