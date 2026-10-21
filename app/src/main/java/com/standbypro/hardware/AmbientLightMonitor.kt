package com.standbypro.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.ln
import kotlin.math.roundToInt

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

    /** Raw illuminance in lux, for adaptive brightness. */
    val lux: Flow<Float> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_LIGHT) trySend(event.values[0])
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (lightSensor != null) sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }.conflate()

    companion object {
        /**
         * Maps room light to screen brightness on a log curve (0 lux → 2%, ~2000 lux → full).
         * The user's brightness slider biases the curve from half to one-and-a-half times, and
         * the result snaps to 2% steps so sensor noise doesn't flicker the panel.
         */
        fun adaptiveBrightness(lux: Float, userLevel: Float): Float {
            val curve = (ln(1f + lux.coerceAtLeast(0f)) / ln(2001f)).coerceIn(0f, 1f)
            val level = (0.02f + 0.98f * curve) * (0.5f + userLevel.coerceIn(0f, 1f))
            return ((level.coerceIn(0.01f, 1f) * 50f).roundToInt() / 50f).coerceIn(0.02f, 1f)
        }
    }
}
