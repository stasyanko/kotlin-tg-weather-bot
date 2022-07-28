package com.weather_bot.checker

import arrow.core.Either
import com.weather_bot.WeatherEnum
import com.weather_bot.weather_provider.WeatherItem
import com.weather_bot.weather_provider.WeatherProviderAdapter
import java.math.BigDecimal
import java.time.Instant

class WeatherChecker(
    private val weatherProvider: WeatherProviderAdapter
) {
    suspend fun matchesOnDay(
        lat: BigDecimal,
        lon: BigDecimal,
        weatherFromUser: WeatherEnum
    ): Either<Error, Instant?> {
        val weatherData = weatherProvider.threeDayForecast(lat, lon)
        return when(weatherData) {
            is Either.Left -> {
                Either.Left(Error("could not fetch weather data"))
            }
            is Either.Right -> {
                val weatherForThreeDays = weatherData.value
                for (weatherItem in weatherForThreeDays) {
                    if(matches(weatherFromUser, weatherItem)) {
                        return Either.Right(weatherItem.dateTime)
                    }
                }

                Either.Right(null)
            }
        }
    }

    private fun matches(
        weatherFromUser: WeatherEnum,
          var tempMatches = false
        var skyMatches = false

        if(
            (weatherFromUser.tempFrom == null &&
                    weatherFromUser.tempTo == null) ||
            (weatherFromUser.tempFrom != null &&
            weatherFromUser.tempTo != null &&
            weatherFromProvider.temp.toInt() in weatherFromUser.tempFrom..weatherFromUser.tempTo)
        ) {
            tempMatches = true
        }      weathe        var tempMatches = false
        var skyMatches = false

        if(
            (weatherFromUser.tempFrom == null &&
                    weatherFromUser.tempTo == null) ||
            (weatherFromUser.tempFrom != null &&
            weatherFromUser.tempTo != null &&
            weatherFromProvider.temp.toInt() in weatherFromUser.tempFrom..weatherFromUser.tempTo)
        ) {
            tempMatches = true
        }rFromProvider: WeatherItem
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