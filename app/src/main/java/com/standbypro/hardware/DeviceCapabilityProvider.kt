package com.standbypro.hardware

import android.os.Build

data class DeviceCapabilities(
    val hasOLED: Boolean,
    val isNothingPhone: Boolean,
    val isTablet: Boolean
)

object DeviceCapabilityProvider {
    
    fun getCapabilities(): DeviceCapabilities {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val model = Build.MODEL.lowercase()
        
        val isNothingPhone = manufacturer == "nothing"
        
        // Approximate for OLED (Nothing phones usually have OLED, Galaxy Tabs vary but let's assume AMOLED for premium models)
        val hasOLED = isNothingPhone || (manufacturer == "samsung" && model.contains("tab"))
        
        // Very basic tablet heuristic (would use resource qualifiers normally)
        val isTablet = model.contains("tab") || model.contains("pad")
        
        return DeviceCapabilities(
            hasOLED = hasOLED,
            isNothingPhone = isNothingPhone,
            isTablet = isTablet
        )
    }
}
