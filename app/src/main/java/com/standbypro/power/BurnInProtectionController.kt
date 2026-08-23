package com.standbypro.power

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

object BurnInProtectionController {
    
    // Max pixel shift in each direction
    private const val MAX_SHIFT_PX = 15f
    // Update interval (e.g., every 1 minute)
    private const val UPDATE_INTERVAL_MS = 60_000L

    val burnInOffset: Flow<Offset> = flow {
        var currentX = 0f
        var currentY = 0f
        
        while (true) {
            emit(Offset(currentX, currentY))
            
            // Wait before next shift
            delay(UPDATE_INTERVAL_MS)
            
            // Calculate next position using a slow random walk
            // We ensure it stays within bounds
            val dx = Random.nextFloat() * 2f - 1f // -1 to 1
            val dy = Random.nextFloat() * 2f - 1f // -1 to 1
            
            currentX = (currentX + dx * 2f).coerceIn(-MAX_SHIFT_PX, MAX_SHIFT_PX)
            currentY = (currentY + dy * 2f).coerceIn(-MAX_SHIFT_PX, MAX_SHIFT_PX)
        }
    }
}
