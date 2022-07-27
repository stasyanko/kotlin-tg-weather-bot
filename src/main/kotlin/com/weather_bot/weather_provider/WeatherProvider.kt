package com.weather_bot.weather_provider

import arrow.core.Either
import com.weather_bot.SkyConditionEnum
import java.math.BigDecimal
import java.time.Instant

data class WeatherItem(
    val dateTime: Instant,
    val temp: Double,
    val skyConditions: List<SkyConditionEnum>,
)

interface IWeatherProvider {
    suspend fun threeDayForecast(lat: BigDecimal, lon: BigDecimal): Either<Error, List<WeatherItem>>
}

class WeatherProviderAdapter(private val openWeatherMapApi: OpenWeatherMapApi): IWeatherProvider {
    override suspend fun threeDayForecast(lat: BigDecimal, lon: BigDecimal): Either<Error, List<WeatherItem>> {
        val threeDayForecast = mutableListOf<WeatherItem>()
        return try {
            val fiveDayForecast = openWeatherMapApi.fiveDayForecast(lat, lon)
            for (threeHourForecast in fiveDayForecast) {
                threeDayForecast.add(WeatherItem(
                    Instant.ofEpochSecond(threeHourForecast.dateTimeUnix.toLong()),
                    threeHourForecast.temp,
                    threeHourForecast.skyCondCodes.mapNotNull { mapSkyCondition(it) },
                ))
            }
            Either.Right(threeDayForecast)
        } catch (e: Exception) {
            Either.Left(Error())
        }
    }

    private fun mapSkyCondition(openWeatherMapSkyCondition: Int): SkyConditionEnum? {
        // can it be more readable?
        // it is not code, it is just plain english!!!
        return when(openWeatherMapSkyCondition) {
            in 200..531 -> {
                SkyConditionEnum.RAIN
            }
            in 600..622 -> {
                SkyConditionEnum.SNOW
            }
            800 -> {
                SkyConditionEnum.CLEAR
            }
            in 801..804 -> {
                SkyConditionEnum.CLOUDY
            }
            else -> null
        }
    }
}