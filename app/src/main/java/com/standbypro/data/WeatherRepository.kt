package com.standbypro.data

import com.standbypro.domain.WeatherData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface WeatherDataSource {
    fun getWeatherDataFlow(): Flow<WeatherData>
    suspend fun refreshWeather()
}

class DemoWeatherDataSource : WeatherDataSource {
    private val _weatherState = MutableStateFlow(DemoDataProvider.demoWeather)

    override fun getWeatherDataFlow(): Flow<WeatherData> = _weatherState.asStateFlow()

    override suspend fun refreshWeather() {
        // Deterministic refresh simulation
        _weatherState.value = DemoDataProvider.demoWeather
    }
}

object WeatherRepository {
    private val dataSource: WeatherDataSource = DemoWeatherDataSource()

    val weatherFlow: Flow<WeatherData> = dataSource.getWeatherDataFlow()

    suspend fun refresh() {
        dataSource.refreshWeather()
    }
}
