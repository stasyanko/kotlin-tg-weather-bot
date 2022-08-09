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
    suspend fun fiveDayForecast(lat: BigDecimal, lon: BigDecimal): Either<WeatherCheckError, List<WeatherItem>>
}

sealed class WeatherCheckError {
    class AuthError(val reason: String) : WeatherCheckError()
    class GeoError(val userId: String): WeatherCheckError()
    object SubscriptionError: WeatherCheckError()
    object NetworkError: WeatherCheckError()
    object ServerError: WeatherCheckError()
}

class WeatherProviderAdapter(private val openWeatherMapApi: OpenWeatherMapApi): IWeatherProvider {
    override suspend fun fiveDayForecast(
        lat: BigDecimal,
        lon: BigDecimal
    ): Either<WeatherCheckError, List<WeatherItem>> {
        val resForecast = mutableListOf<WeatherItem>()
        return try {
            val fiveDayForecast = openWeatherMapApi.fiveDayForecast(lat, lon)
            for (threeHourForecast in fiveDayForecast) {
                resForecast.add(WeatherItem(
                    Instant.ofEpochSecond(threeHourForecast.dateTimeUnix.toLong()),
                    threeHourForecast.temp,
                    threeHourForecast.skyCondCodes.mapNotNull { mapSkyCondition(it) },
                ))
            }
            Either.Right(resForecast)
        } catch (e: AuthException) {
            Either.Left(WeatherCheckError.AuthError(e.message.let { it ?: "Auth error" }))
        } catch (e: GeoException) {
            Either.Left(WeatherCheckError.GeoError(e.message.let { it ?: "Auth error" }))
        } catch (e: SubscriptionException) {
            Either.Left(WeatherCheckError.SubscriptionError)
        } catch (e: ServerErrorException) {
            Either.Left(WeatherCheckError.ServerError)
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