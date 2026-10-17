package com.standbypro.data

import com.standbypro.domain.TimeZoneClock
import java.time.ZoneId

/** A named place with coordinates, used for weather, sun/moon maths and world clocks. */
data class Place(
    val city: String,
    val country: String,
    val zoneId: String,
    val latitude: Double,
    val longitude: Double
) {
    fun toClock(): TimeZoneClock = TimeZoneClock(zoneId, city, country)
}

/**
 * Curated list of major cities. Used to pick a sensible default location from the device
 * time zone (so weather works on first launch without any permission) and as the world
 * clock city catalogue.
 */
object Locations {

    val places: List<Place> = listOf(
        Place("New Delhi", "India", "Asia/Kolkata", 28.6139, 77.2090),
        Place("Mumbai", "India", "Asia/Kolkata", 19.0760, 72.8777),
        Place("Bengaluru", "India", "Asia/Kolkata", 12.9716, 77.5946),
        Place("London", "United Kingdom", "Europe/London", 51.5072, -0.1276),
        Place("Paris", "France", "Europe/Paris", 48.8566, 2.3522),
        Place("Berlin", "Germany", "Europe/Berlin", 52.5200, 13.4050),
        Place("Madrid", "Spain", "Europe/Madrid", 40.4168, -3.7038),
        Place("Rome", "Italy", "Europe/Rome", 41.9028, 12.4964),
        Place("Amsterdam", "Netherlands", "Europe/Amsterdam", 52.3676, 4.9041),
        Place("Zurich", "Switzerland", "Europe/Zurich", 47.3769, 8.5417),
        Place("Stockholm", "Sweden", "Europe/Stockholm", 59.3293, 18.0686),
        Place("Istanbul", "Türkiye", "Europe/Istanbul", 41.0082, 28.9784),
        Place("Moscow", "Russia", "Europe/Moscow", 55.7558, 37.6173),
        Place("Dubai", "UAE", "Asia/Dubai", 25.2048, 55.2708),
        Place("Riyadh", "Saudi Arabia", "Asia/Riyadh", 24.7136, 46.6753),
        Place("Cairo", "Egypt", "Africa/Cairo", 30.0444, 31.2357),
        Place("Nairobi", "Kenya", "Africa/Nairobi", -1.2921, 36.8219),
        Place("Lagos", "Nigeria", "Africa/Lagos", 6.5244, 3.3792),
        Place("Johannesburg", "South Africa", "Africa/Johannesburg", -26.2041, 28.0473),
        Place("Karachi", "Pakistan", "Asia/Karachi", 24.8607, 67.0011),
        Place("Dhaka", "Bangladesh", "Asia/Dhaka", 23.8103, 90.4125),
        Place("Kathmandu", "Nepal", "Asia/Kathmandu", 27.7172, 85.3240),
        Place("Colombo", "Sri Lanka", "Asia/Colombo", 6.9271, 79.8612),
        Place("Bangkok", "Thailand", "Asia/Bangkok", 13.7563, 100.5018),
        Place("Jakarta", "Indonesia", "Asia/Jakarta", -6.2088, 106.8456),
        Place("Singapore", "Singapore", "Asia/Singapore", 1.3521, 103.8198),
        Place("Kuala Lumpur", "Malaysia", "Asia/Kuala_Lumpur", 3.1390, 101.6869),
        Place("Hong Kong", "China", "Asia/Hong_Kong", 22.3193, 114.1694),
        Place("Shanghai", "China", "Asia/Shanghai", 31.2304, 121.4737),
        Place("Beijing", "China", "Asia/Shanghai", 39.9042, 116.4074),
        Place("Taipei", "Taiwan", "Asia/Taipei", 25.0330, 121.5654),
        Place("Seoul", "South Korea", "Asia/Seoul", 37.5665, 126.9780),
        Place("Tokyo", "Japan", "Asia/Tokyo", 35.6762, 139.6503),
        Place("Manila", "Philippines", "Asia/Manila", 14.5995, 120.9842),
        Place("Sydney", "Australia", "Australia/Sydney", -33.8688, 151.2093),
        Place("Melbourne", "Australia", "Australia/Melbourne", -37.8136, 144.9631),
        Place("Perth", "Australia", "Australia/Perth", -31.9505, 115.8605),
        Place("Auckland", "New Zealand", "Pacific/Auckland", -36.8485, 174.7633),
        Place("Honolulu", "USA", "Pacific/Honolulu", 21.3069, -157.8583),
        Place("Anchorage", "USA", "America/Anchorage", 61.2181, -149.9003),
        Place("Los Angeles", "USA", "America/Los_Angeles", 34.0522, -118.2437),
        Place("San Francisco", "USA", "America/Los_Angeles", 37.7749, -122.4194),
        Place("Seattle", "USA", "America/Los_Angeles", 47.6062, -122.3321),
        Place("Vancouver", "Canada", "America/Vancouver", 49.2827, -123.1207),
        Place("Denver", "USA", "America/Denver", 39.7392, -104.9903),
        Place("Phoenix", "USA", "America/Phoenix", 33.4484, -112.0740),
        Place("Chicago", "USA", "America/Chicago", 41.8781, -87.6298),
        Place("Mexico City", "Mexico", "America/Mexico_City", 19.4326, -99.1332),
        Place("Toronto", "Canada", "America/Toronto", 43.6532, -79.3832),
        Place("New York", "USA", "America/New_York", 40.7128, -74.0060),
        Place("Miami", "USA", "America/New_York", 25.7617, -80.1918),
        Place("Bogotá", "Colombia", "America/Bogota", 4.7110, -74.0721),
        Place("Lima", "Peru", "America/Lima", -12.0464, -77.0428),
        Place("Santiago", "Chile", "America/Santiago", -33.4489, -70.6693),
        Place("Buenos Aires", "Argentina", "America/Argentina/Buenos_Aires", -34.6037, -58.3816),
        Place("São Paulo", "Brazil", "America/Sao_Paulo", -23.5505, -46.6333),
        Place("Reykjavík", "Iceland", "Atlantic/Reykjavik", 64.1466, -21.9426),
        Place("UTC", "Coordinated Universal Time", "UTC", 51.4779, -0.0015)
    )

    /** Best guess of the user's city from the device time zone. */
    fun defaultPlace(zone: ZoneId = ZoneId.systemDefault()): Place {
        val id = zone.id
        return places.firstOrNull { it.zoneId == id }
            ?: places.firstOrNull { sameOffset(it.zoneId, id) }
            ?: places.first { it.zoneId == "UTC" }
    }

    fun byZone(zoneId: String): Place? = places.firstOrNull { it.zoneId == zoneId }

    fun byCity(city: String): Place? = places.firstOrNull { it.city.equals(city, ignoreCase = true) }

    private fun sameOffset(a: String, b: String): Boolean = try {
        val now = java.time.Instant.now()
        ZoneId.of(a).rules.getOffset(now) == ZoneId.of(b).rules.getOffset(now)
    } catch (e: Exception) {
        false
    }

    val defaultWorldCities = listOf("London", "New York", "Tokyo", "Dubai", "Sydney", "San Francisco")
}
