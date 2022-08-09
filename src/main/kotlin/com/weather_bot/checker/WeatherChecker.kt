package com.weather_bot.checker

import arrow.core.Either
import com.weather_bot.WeatherEnum
import com.weather_bot.weather_provider.WeatherCheckError
import com.weather_bot.weather_provider.WeatherItem
import com.weather_bot.weather_provider.WeatherProviderAdapter
import java.math.BigDecimal
import java.time.Instant

sealed class MatchedInstant {
    class NotEmptyInstant(val instant: Instant): MatchedInstant()
    object EmptyInstant: MatchedInstant()
}

@JvmInline
value class Lat(val lat: BigDecimal)
@JvmInline
value class Lon(val lon: BigDecimal)

class WeatherChecker(
    private val weatherProvider: WeatherProviderAdapter
) {
    suspend fun matchesOnDay(
        //TODO: refactor lat and lon to value classes
        lat: Lat,
        lon: Lon,
        weatherFromUser: WeatherEnum
    ): Either<WeatherCheckError, MatchedInstant> {
        val weatherData = weatherProvider.fiveDayForecast(lat.lat, lon.lon)
        return when(weatherData) {
            is Either.Left -> {
                Either.Left(weatherData.value)
            }
            is Either.Right -> {
                val weatherForThreeDays = weatherData.value
                for (weatherItem in weatherForThreeDays) {
                    if(matches(weatherFromUser, weatherItem)) {
                        return Either.Right(MatchedInstant.NotEmptyInstant(weatherItem.dateTime))
                    }
                }

                Either.Right(MatchedInstant.EmptyInstant)
            }
        }
    }

    private fun matches(
        weatherFromUser: WeatherEnum,
        weatherFromProvider: WeatherItem
    ): Boolean {
        var tempMatches = false
        var skyMatches = false
        //TODO: refactor to when without argument and make temp not nullable with range from -273 to 60
        if(
            (weatherFromUser.tempFrom == null &&
                    weatherFromUser.tempTo == null) ||
            (weatherFromUser.tempFrom != null &&
            weatherFromUser.tempTo != null &&
            weatherFromProvider.temp.toInt() in weatherFromUser.tempFrom..weatherFromUser.tempTo)
        ) {
            tempMatches = true
        }

        if(
            weatherFromUser.skyCondition == null ||
            weatherFromProvider.skyConditions.contains(weatherFromUser.skyCondition)
        ) {
            skyMatches = true
        }

        return tempMatches && skyMatches
    }
}