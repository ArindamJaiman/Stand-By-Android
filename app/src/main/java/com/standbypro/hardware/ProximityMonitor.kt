package com.standbypro.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Emits once per hand wave over the proximity sensor. Registered only while collected. */
class ProximityMonitor(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    val hasSensor: Boolean get() = sensor != null

    val waves: Flow<Unit> = callbackFlow {
        val proximity = sensor
        if (proximity == null) {
            awaitClose { }
            return@callbackFlow
        }
        val detector = WaveDetector()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val near = event.values[0] < proximity.maximumRange.coerceAtMost(5f)
                if (detector.onSample(near, SystemClock.elapsedRealtime())) trySend(Unit)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, proximity, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
