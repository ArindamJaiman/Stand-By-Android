package com.standbypro.astro

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Offline astronomy used by the Moon, Solar, Sunrise and Orrery faces. Accuracy is a few
 * minutes for sun events and a few hours for lunar phases – plenty for a bedside display,
 * and it works without network or location permission.
 */
object Astronomy {

    const val SYNODIC_MONTH = 29.530588853
    private const val KNOWN_NEW_MOON_JD = 2451550.1 // 2000-01-06 18:14 UTC
    private const val J2000 = 2451545.0
    private const val DEG = PI / 180.0

    fun julianDay(epochMillis: Long): Double = epochMillis / 86_400_000.0 + 2440587.5

    fun epochMillis(julianDay: Double): Long = ((julianDay - 2440587.5) * 86_400_000.0).toLong()

    data class MoonPhase(
        /** 0 = new, 0.25 = first quarter, 0.5 = full, 0.75 = last quarter. */
        val fraction: Double,
        val illumination: Double,
        val ageDays: Double,
        val name: String,
        val waxing: Boolean
    )

    fun moonPhase(epochMillis: Long = System.currentTimeMillis()): MoonPhase {
        val days = julianDay(epochMillis) - KNOWN_NEW_MOON_JD
        var f = (days / SYNODIC_MONTH) % 1.0
        if (f < 0) f += 1.0
        val illumination = (1 - cos(2 * PI * f)) / 2
        val name = when {
            f < 0.0339 || f >= 0.9661 -> "New Moon"
            f < 0.2161 -> "Waxing Crescent"
            f < 0.2839 -> "First Quarter"
            f < 0.4661 -> "Waxing Gibbous"
            f < 0.5339 -> "Full Moon"
            f < 0.7161 -> "Waning Gibbous"
            f < 0.7839 -> "Last Quarter"
            else -> "Waning Crescent"
        }
        return MoonPhase(f, illumination, f * SYNODIC_MONTH, name, f < 0.5)
    }

    /** Next instant (epoch ms) at which the moon reaches [targetFraction] (0.5 = full). */
    fun nextPhase(targetFraction: Double, fromMillis: Long = System.currentTimeMillis()): Long {
        val current = moonPhase(fromMillis).fraction
        var delta = targetFraction - current
        if (delta <= 0.001) delta += 1.0
        return fromMillis + (delta * SYNODIC_MONTH * 86_400_000.0).toLong()
    }

    data class SunTimes(
        val sunrise: LocalDateTime?,
        val sunset: LocalDateTime?,
        val solarNoon: LocalDateTime,
        val polarDay: Boolean,
        val polarNight: Boolean
    ) {
        val dayLengthMinutes: Long
            get() = if (sunrise != null && sunset != null) {
                java.time.Duration.between(sunrise, sunset).toMinutes()
            } else if (polarDay) 1440 else 0
    }

    /** Sunrise equation (NOAA simplified). Longitude east positive. */
    fun sunTimes(date: LocalDate, latitude: Double, longitude: Double, zone: ZoneId = ZoneId.systemDefault()): SunTimes {
        val jdNoon = date.toEpochDay() + 2440587.5 + 0.5
        val n = floor(jdNoon - J2000 + 0.0008)
        val jStar = n - longitude / 360.0
        val m = ((357.5291 + 0.98560028 * jStar) % 360 + 360) % 360
        val c = 1.9148 * sin(m * DEG) + 0.02 * sin(2 * m * DEG) + 0.0003 * sin(3 * m * DEG)
        val lambda = ((m + c + 180 + 102.9372) % 360 + 360) % 360
        val jTransit = J2000 + jStar + 0.0053 * sin(m * DEG) - 0.0069 * sin(2 * lambda * DEG)
        val sinDec = sin(lambda * DEG) * sin(23.4397 * DEG)
        val cosDec = cos(asin(sinDec))
        val cosOmega = (sin(-0.833 * DEG) - sin(latitude * DEG) * sinDec) / (cos(latitude * DEG) * cosDec)
        val noon = toLocal(jTransit, zone)
        return when {
            cosOmega < -1 -> SunTimes(null, null, noon, polarDay = true, polarNight = false)
            cosOmega > 1 -> SunTimes(null, null, noon, polarDay = false, polarNight = true)
            else -> {
                val omega = acos(cosOmega) / DEG
                SunTimes(
                    sunrise = toLocal(jTransit - omega / 360.0, zone),
                    sunset = toLocal(jTransit + omega / 360.0, zone),
                    solarNoon = noon,
                    polarDay = false,
                    polarNight = false
                )
            }
        }
    }

    /** Solar elevation in degrees (negative below horizon). */
    fun sunElevation(epochMillis: Long, latitude: Double, longitude: Double): Double {
        val d = julianDay(epochMillis) - J2000
        val g = ((357.529 + 0.98560028 * d) % 360) * DEG
        val q = (280.459 + 0.98564736 * d) % 360
        val l = (q + 1.915 * sin(g) + 0.020 * sin(2 * g)) * DEG
        val e = (23.439 - 0.00000036 * d) * DEG
        val dec = asin(sin(e) * sin(l))
        val ra = kotlin.math.atan2(cos(e) * sin(l), cos(l)) / DEG
        val gmst = (18.697374558 + 24.06570982441908 * d) % 24
        var hourAngle = (gmst * 15 + longitude - ra) % 360
        if (hourAngle < -180) hourAngle += 360
        if (hourAngle > 180) hourAngle -= 360
        val lat = latitude * DEG
        val alt = asin(sin(lat) * sin(dec) + cos(lat) * cos(dec) * cos(hourAngle * DEG))
        return alt / DEG
    }

    private fun toLocal(jd: Double, zone: ZoneId): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis(jd)), zone)

    data class Planet(val name: String, val l0: Double, val rate: Double, val orbitAu: Double, val colorArgb: Long, val sizeScale: Float)

    /** Mean orbital elements at J2000 (degrees, degrees/day). */
    val planets = listOf(
        Planet("Mercury", 252.25084, 4.09233445, 0.39, 0xFFB7B1A8, 0.45f),
        Planet("Venus", 181.97973, 1.60213034, 0.72, 0xFFE8C98A, 0.7f),
        Planet("Earth", 100.46435, 0.98560910, 1.0, 0xFF4FA3FF, 0.75f),
        Planet("Mars", 355.45332, 0.52403304, 1.52, 0xFFE0643C, 0.55f),
        Planet("Jupiter", 34.40438, 0.08308676, 5.2, 0xFFD9B48A, 1.25f),
        Planet("Saturn", 49.94432, 0.03346063, 9.54, 0xFFE6D3A3, 1.1f),
        Planet("Uranus", 313.23218, 0.01173129, 19.19, 0xFF9FE3E8, 0.85f),
        Planet("Neptune", 304.88003, 0.00598106, 30.07, 0xFF5B7CFF, 0.85f)
    )

    /** Heliocentric mean longitude in degrees [0, 360). */
    fun meanLongitude(planet: Planet, epochMillis: Long): Double {
        val d = julianDay(epochMillis) - J2000
        val l = (planet.l0 + planet.rate * d) % 360
        return if (l < 0) l + 360 else l
    }

    fun isAboutEqual(a: Double, b: Double, tolerance: Double): Boolean = abs(a - b) <= tolerance
}
