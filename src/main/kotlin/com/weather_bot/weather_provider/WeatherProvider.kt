package com.weather_bot.weather_provider

import com.weather_bot.SkyConditionEnum
import java.time.Instant

data class OneDayWeatherItem(
    val dateTime: Instant,
    val temp: Double,
    val skyCondition: SkyConditionEnum,
)

interface IWeatherProvider {
    suspend fun threeDayForecast(): List<OneDayWeatherItem>
}

class WeatherProvider: IWeatherProvider {
    override suspend fun threeDayForecast(): List<OneDayWeatherItem> {
        TODO("Not yet implemented")
    }
}