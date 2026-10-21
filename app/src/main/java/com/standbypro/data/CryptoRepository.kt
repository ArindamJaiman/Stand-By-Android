package com.standbypro.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

data class CoinPrice(val id: String, val symbol: String, val name: String, val usd: Double, val change24h: Double) {
    val priceLabel: String
        get() {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            format.maximumFractionDigits = when {
                usd >= 10_000 -> 0
                usd >= 1 -> 2
                else -> 4
            }
            format.minimumFractionDigits = format.maximumFractionDigits
            return format.format(usd)
        }

    val changeLabel: String get() = (if (change24h >= 0) "▲ " else "▼ ") + "%.2f%%".format(Locale.US, kotlin.math.abs(change24h))
}

/** Live crypto prices from CoinGecko's public price endpoint (no key needed). */
object CryptoRepository {

    val COINS = listOf(
        Triple("bitcoin", "BTC", "Bitcoin"),
        Triple("ethereum", "ETH", "Ethereum"),
        Triple("solana", "SOL", "Solana")
    )

    /** Shown in gallery previews and demo mode before the first fetch. */
    val SAMPLE = listOf(
        CoinPrice("bitcoin", "BTC", "Bitcoin", 67_412.0, 1.84),
        CoinPrice("ethereum", "ETH", "Ethereum", 3_286.5, -0.62),
        CoinPrice("solana", "SOL", "Solana", 168.42, 4.27)
    )

    private const val REFRESH_INTERVAL_MS = 10 * 60 * 1000L

    private val _prices = MutableStateFlow<List<CoinPrice>>(emptyList())
    val prices: StateFlow<List<CoinPrice>> = _prices.asStateFlow()

    private var lastFetchMillis = 0L

    suspend fun refresh(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastFetchMillis < REFRESH_INTERVAL_MS && _prices.value.isNotEmpty()) return
        val url = "https://api.coingecko.com/api/v3/simple/price?ids=${COINS.joinToString(",") { it.first }}&vs_currencies=usd&include_24hr_change=true"
        try {
            val json = withContext(Dispatchers.IO) { WeatherRepository.download(url) } ?: return
            _prices.value = parse(json)
            lastFetchMillis = now
        } catch (e: Exception) {
            Log.w("CryptoRepository", "Price refresh failed: ${e.message}")
        }
    }

    internal fun parse(json: String): List<CoinPrice> {
        val root = JSONObject(json)
        return COINS.mapNotNull { (id, symbol, name) ->
            val o = root.optJSONObject(id) ?: return@mapNotNull null
            CoinPrice(id, symbol, name, o.optDouble("usd"), o.optDouble("usd_24h_change", 0.0))
        }.filter { !it.usd.isNaN() }
    }
}
