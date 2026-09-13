package com.standbypro.power

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

data class BurnInOffset(val x: Float, val y: Float)

object BurnInProtectionController {
    
    private const val MAX_SHIFT_PX = 15f
    private const val UPDATE_INTERVAL_MS = 60_000L // 1 minute

    val burnInOffset: Flow<BurnInOffset> = flow {
        var currentX = 0f
        var currentY = 0f
        
        while (true) {
            emit(BurnInOffset(currentX, currentY))
            delay(UPDATE_INTERVAL_MS)
            
            val dx = Random.nextFloat() * 2f - 1f
            val dy = Random.nextFloat() * 2f - 1f
            
            currentX = (currentX + dx * 2f).coerceIn(-MAX_SHIFT_PX, MAX_SHIFT_PX)
            currentY = (currentY + dy * 2f).coerceIn(-MAX_SHIFT_PX, MAX_SHIFT_PX)
        }
    }
}
