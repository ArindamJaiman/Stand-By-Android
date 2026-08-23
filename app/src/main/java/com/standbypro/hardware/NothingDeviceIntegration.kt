package com.standbypro.hardware

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

object NothingDeviceIntegration {
    
    // Nothing OS aesthetics favor dot matrix fonts and monochrome colors.
    // In a real app we could load the Nothing font from assets.
    
    val nothingRed = Color(0xFFEA3323)
    val nothingMonochrome = Color.White
    
    fun getNothingFontFamily(): FontFamily {
        // Fallback to default sans serif if custom font not available
        return FontFamily.SansSerif 
    }
}
