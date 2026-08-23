package com.standbypro.hardware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.standbypro.domain.BatteryHealth
import com.standbypro.domain.ChargeType
import com.standbypro.domain.ChargingState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ChargingStateMonitor(private val context: Context) {

    val chargingState: Flow<ChargingState> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    trySend(getChargingStateFromIntent(it))
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(receiver, filter)

        // Send initial state
        initialIntent?.let {
            trySend(getChargingStateFromIntent(it))
        }

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    private fun getChargingStateFromIntent(intent: Intent): ChargingState {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPercent = if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            0
        }

        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val chargeType = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> ChargeType.USB
            BatteryManager.BATTERY_PLUGGED_AC -> ChargeType.AC
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargeType.WIRELESS
            else -> ChargeType.NONE
        }

        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val temperatureCelsius = if (temperature != -1) temperature / 10f else null

        val healthExtra = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val health = when (healthExtra) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            else -> BatteryHealth.UNKNOWN
        }

        return ChargingState(
            isCharging = isCharging,
            batteryPercent = batteryPercent,
            chargeType = chargeType,
            temperatureCelsius = temperatureCelsius,
            health = health
        )
    }
}
