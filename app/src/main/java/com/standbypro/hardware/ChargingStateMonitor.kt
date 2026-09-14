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
import kotlinx.coroutines.flow.distinctUntilChanged

class ChargingStateMonitor(private val context: Context) {

    val chargingState: Flow<ChargingState> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    trySend(getChargingStateFromIntent(it))
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        val initialIntent = context.registerReceiver(receiver, filter)

        // Send initial state
        val stickyIntent = initialIntent ?: context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        stickyIntent?.let {
            trySend(getChargingStateFromIntent(it))
        }

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.distinctUntilChanged()

    private fun getChargingStateFromIntent(intent: Intent): ChargingState {
        val action = intent.action
        val isExplicitDisconnect = action == Intent.ACTION_POWER_DISCONNECTED
        val isExplicitConnect = action == Intent.ACTION_POWER_CONNECTED

        val batteryIntent = if (action == Intent.ACTION_BATTERY_CHANGED) {
            intent
        } else {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: intent
        }

        val chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val isPlugged = chargePlug == BatteryManager.BATTERY_PLUGGED_USB ||
                chargePlug == BatteryManager.BATTERY_PLUGGED_AC ||
                chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS ||
                chargePlug == 8 // Dock

        val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isChargingFromStatus = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val isCharging = when {
            isExplicitDisconnect -> false
            isExplicitConnect -> true
            !isPlugged -> false
            else -> isChargingFromStatus
        }

        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPercent = if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            0
        }

        val chargeType = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> ChargeType.USB
            BatteryManager.BATTERY_PLUGGED_AC -> ChargeType.AC
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargeType.WIRELESS
            else -> if (isCharging) ChargeType.AC else ChargeType.NONE
        }

        val temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val temperatureCelsius = if (temperature != -1) temperature / 10f else null

        val healthExtra = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
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
