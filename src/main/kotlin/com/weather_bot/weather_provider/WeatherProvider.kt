package com.weather_bot.weather_provider

import com.weather_bot.SkyConditionEnum
import java.math.BigDecimal
import java.time.Instant

data class WeatherItem(
    val dateTime: Instant,
    val temp: Double,
    val skyConditions: List<SkyConditionEnum>,
)

interface IWeatherProvider {
    suspend fun threeDayForecast(lat: BigDecimal, lon: BigDecimal): List<WeatherItem>
}

class WeatherProviderAdapter(private val openWeatherMapApi: OpenWeatherMapApi): IWeatherProvider {
    override suspend fun threeDayForecast(lat: BigDecimal, lon: BigDecimal): List<WeatherItem> {
        val threeDayForecast = mutableListOf<WeatherItem>()
        val fiveDayForecast = openWeatherMapApi.fiveDayForecast(lat, lon)
        for (threeHourForecast in fiveDayForecast) {
            val skyConds = threeHourForecast.skyCondCodes.mapNotNull { mapSkyCondition(it) }

            threeDayForecast.add(WeatherItem(
                Instant.ofEpochSecond(threeHourForecast.dateTimeUnix.toLong()),
                threeHourForecast.temp,
                skyConds,
            ))
        }
        return threeDayForecast
    }

    private fun mapSkyCondition(openWeatherMapSkyCondition: Int): SkyConditionEnum? {
        // can it be more readable?
        // it is not code, it is plain english!!!
        return when(openWeatherMapSkyCondition) {
            in 200..531 -> {
                SkyConditionEnum.RAIN
            }
            in 600..622 -> {
                SkyConditionEnum.SNOW
            }
            else -> null
        }
    }
}