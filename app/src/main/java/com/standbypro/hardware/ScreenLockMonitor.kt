package com.standbypro.hardware

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class ScreenLockMonitor(private val context: Context) {

    private val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    fun isDeviceLockedOrScreenOff(): Boolean {
        val isScreenOff = !powerManager.isInteractive
        val isLocked = keyguardManager.isKeyguardLocked
        return isScreenOff || isLocked
    }

    val isScreenLocked: Flow<Boolean> = callbackFlow {
        // Emit initial state
        trySend(isDeviceLockedOrScreenOff())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        trySend(true)
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        // User unlocked their phone
                        trySend(false)
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        trySend(isDeviceLockedOrScreenOff())
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }

        context.registerReceiver(receiver, filter)

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.distinctUntilChanged()
}
