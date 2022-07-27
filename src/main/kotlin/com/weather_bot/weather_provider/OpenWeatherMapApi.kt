package com.weather_bot.weather_provider

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger

data class ThreeHourWeatherItem(
    val dateTimeUnix: BigInteger,
    val temp: Double,
    val pressure: Int,
    val humidity: Int,
    val skyCondCodes: List<Int>,
    val windSpeed: Double,
)

class AuthException : Exception() {}
class GeoException : Exception() {}
class SubscriptionException : Exception() {}
class ServerErrorException : Exception() {}

class OpenWeatherMapApi(
    private val httpClient: HttpClient,
    private val appId: String,
) {
    suspend fun fiveDayForecast(lat: BigDecimal, lon: BigDecimal): List<ThreeHourWeatherItem> {
        val res = httpClient.get(
            "https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&appid=${appId}&units=metric"
        )

        res.status.let {
            when(it.value) {
                HttpStatusCode.Unauthorized.value -> {
                    throw AuthException();
                }
                HttpStatusCode.NotFound.value -> {
                    throw GeoException();
                }
                HttpStatusCode.TooManyRequests.value -> {
                    throw SubscriptionException();
                }
                in 500..504 -> {
                    throw ServerErrorException();
                }
                else -> {}
            }
        }

        val fiveDaysWeather = JSONObject(res.bodyAsText()).get("list") as JSONArray

        val result = mutableListOf<ThreeHourWeatherItem>()
        for (day in fiveDaysWeather) {
            val weatherData = day as JSONObject
            val main = weatherData.get("main") as JSONObject
            val wind = weatherData.get("wind") as JSONObject
            val weatherArr = weatherData.get("weather") as JSONArray

            result.add(ThreeHourWeatherItem(
                weatherData.getBigInteger("dt"),
                main.getDouble("temp"),
                main.getInt("pressure"),
                main.getInt("humidity"),
                weatherArr.map { (it as JSONObject).getInt("id") }.toList(),
                wind.getDouble("speed"),
            ))
        }
        return result
    }
}