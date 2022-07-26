package com.weather_bot.weather_provider

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

data class WeatherItem(
    val temp: Double,
    val pressure: Int,
    val humidity: Int,
    val skyCondCodes: List<Int>,
    val windSpeed: Double,
)

class OpenWeatherMapApi(
    private val httpClient: HttpClient,
    private val appId: String,
) {
    suspend fun fiveDayForecast(lat: BigDecimal, lon: BigDecimal): List<WeatherItem> {
        val res = httpClient.get(
            "https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&appid=${appId}"
        )
        if(res.status != HttpStatusCode.OK) {
            println(res.status)
            throw Exception("An error occurred");
        }
        val weatherByDays = JSONObject(res.bodyAsText()).get("list") as JSONArray

        val result = mutableListOf<WeatherItem>()
        for (day in weatherByDays) {
            val weatherData = day as JSONObject
            val main = weatherData.get("main") as JSONObject
            val wind = weatherData.get("wind") as JSONObject
            val weatherArr = weatherData.get("weather") as JSONArray

            result.add(WeatherItem(
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