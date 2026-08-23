package com.standbypro

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.standbypro.services.StandByService
import com.standbypro.theme.StandByProTheme
import com.standbypro.ui.main.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start background service
        val serviceIntent = Intent(this, StandByService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        val isStandByMode = intent.getBooleanExtra("EXTRA_START_STANDBY", false)

        if (isStandByMode) {
            // Setup immersive mode for StandBy
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }
        }

        enableEdgeToEdge()
        setContent {
            StandByProTheme { 
                Surface(
                    modifier = Modifier.fillMaxSize(), 
                    color = MaterialTheme.colorScheme.background
                ) { 
                    if (isStandByMode) {
                        // Placeholder for StandByUI, showing MainScreen for now
                        MainScreen() 
                    } else {
                        // Settings / Preview UI
                        MainScreen()
                    }
                } 
            }
        }
    }
}
