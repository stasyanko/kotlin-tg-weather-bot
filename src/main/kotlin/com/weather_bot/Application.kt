package com.weather_bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import com.pengrad.telegrambot.request.SendMessage
import com.weather_bot.database.PgDatabase
import com.weather_bot.database.User
import com.weather_bot.database.users
import com.weather_bot.weather_provider.OpenWeatherMapApi
import com.weather_bot.weather_provider.WeatherProviderAdapter
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import java.math.BigDecimal
import java.time.Instant
import java.time.Period
import java.time.ZoneOffset
import java.util.*

typealias chatId = Long
typealias stepNumber = Int

enum class StepEnum(val value: stepNumber) {
    WEATHER(1),
    LOCATION(2),
    TIME(3),
}

enum class SkyConditionEnum(val value: Int) {
    RAIN(1),
    SNOW(2),
    CLOUDY(3),
    CLEAR(4),
}

enum class WeatherEnum(
    val id: Int,
    val tempFrom: Int?,
    val tempTo: Int?,
    val skyCondition: SkyConditionEnum?
) {
    HOT(1, 25, 80, null),
    COLD(2, -70, -10, null),
    RAIN(3, null, null, SkyConditionEnum.RAIN),
    SNOW(4, null, null, SkyConditionEnum.SNOW);

    companion object {
        fun labels() = values().map { it.toString() }
        fun fromLabel(label: String): WeatherEnum? = WeatherEnum.values().find { it.name == label }
    }
}

private const val INIT_SCRIPT = "database/init-pg-data.sql"

fun main() {
    val dotenv = Dotenv.load()

    val pgDatabase = PgDatabase()
    val db = pgDatabase.connect(dotenv["DB_USER"], dotenv["DB_PASS"])
    pgDatabase.execSqlScript(INIT_SCRIPT, db)

    val bot = TelegramBot(dotenv["TG_BOT_TOKEN"])
    val chatSteps = Collections.synchronizedMap(mutableMapOf<chatId, StepEnum>())
    //TODO: move it to a runBot() function
    bot.setUpdatesListener { updates: List<Update?>? ->
        updates?.forEach { it ->
            val chatId: Long? = it?.message()?.chat()?.id()
            val userId = chatId.toString()
            val msgText: String? = it?.message()?.text()

            upsertUser(
                db = db,
                userId = userId,
                createdOn = Instant.now()
            )

            when(msgText) {
                "/start" -> {
                    chatSteps[chatId] = StepEnum.WEATHER
                    weatherStepKeyboard(bot, chatId)
                }
                else -> {
                    chatSteps[chatId].let { stepNumber ->
                        when (stepNumber) {
                            StepEnum.WEATHER -> {
                                val weatherEnumVal = msgText?.let { msg -> WeatherEnum.fromLabel(msg) }
                                if(weatherEnumVal == null) {
                                    chatSteps[chatId] = StepEnum.WEATHER
                                    bot.execute(
                                        SendMessage(chatId, "Invalid value for weather: $msgText")
                                    )
                                    weatherStepKeyboard(bot, chatId)
                                    return@let
                                }
                                upsertUser(
                                    db,
                                    userId,
                                    weatherActionId = weatherEnumVal.id
                                )
                                chatSteps[chatId] = StepEnum.LOCATION
                                bot.execute(
                                    SendMessage(chatId, "Please provide your location")
                                )
                            }
                            StepEnum.LOCATION -> {
                                val locationValue = msgText ?: ""
                                val locationRegex = "^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)".toRegex()
                                if(!(locationValue matches locationRegex)) {
                                    bot.execute(
                                        SendMessage(chatId, "Please provide valid location")
                                    )
                                    return@let
                                }

                                val (lat, lng) = locationValue.split(",").toList()
                                upsertUser(
                                    db,
                                    userId,
                                    lat = lat.trim().toBigDecimal(),
                                    lng = lng.trim().toBigDecimal(),
                                )
                                chatSteps[chatId] = StepEnum.TIME
                                bot.execute(
                                    SendMessage(
                                        chatId,
                                        "Please provide an hour for sending a notification (UTC)"
                                    )
                                )
                            }
                            StepEnum.TIME -> {
                                val notifyAtHour = msgText?.toIntOrNull()
                                if(notifyAtHour == null) {
                                    bot.execute(
                                        SendMessage(chatId, "A value for hour must be numeric")
                                    )
                                    return@let
                                }
                                if(notifyAtHour !in 0..23) {
                                    bot.execute(
                                        SendMessage(
                                            chatId,
                                            "A value for hour must be between 0 and 23"
                                        )
                                    )
                                    return@let
                                }

                                upsertUser(
                                    db,
                                    userId,
                                    notifyAtHour = notifyAtHour
                                )
                                bot.execute(
                                    SendMessage(
                                        chatId,
                                        "Well done! You will receive notifications for the selected weather!"
                                    )
                                )
                            }
                            null -> {
                                println("empty step")
                            }
                        }
                    }
                }
            }
        }

        UpdatesListener.CONFIRMED_UPDATES_ALL
    }



    //TODO: say why Timer in jvm is better than crontab
    Timer().scheduleAtFixedRate(object : TimerTask() {
        private val coroutineScope = CoroutineScope(Dispatchers.Default)

        override fun run() {
            //1. Get all users for checking weather
            //2. Pass them to WeatherChecker
            //3. Send a notification if needed
            val curTime = Instant.now().atZone(ZoneOffset.UTC)
            val curHourUtc = curTime.hour
            val dayAgoTime = Instant.from(curTime).minus(Period.ofDays(1))
            val users = db.users.filter {
                (it.notifyAtHour eq curHourUtc) and
                (it.lastNotified.isNull() or (it.lastNotified lt dayAgoTime))
            }.toList()
            coroutineScope.launch {
//                users.forEach { user ->
//
//                }
            }
        }
    }, 0,3000)
}

private fun weatherStepKeyboard(bot: TelegramBot, chatId: Long?) {
    val weatherButtons = WeatherEnum.labels().map { KeyboardButton(it) }.toTypedArray()
    bot.execute(SendMessage(chatId, "Please, select a weather condition").replyMarkup(
        ReplyKeyboardMarkup(weatherButtons)
    ))
}

private fun upsertUser(
    db: Database,
    userId: String,
    weatherActionId: Int? = null,
    lat: BigDecimal? = null,
    lng: BigDecimal? = null,
    notifyAtHour: Int? = null,
    lastNotified: Instant? = null,
    createdOn: Instant? = null
): User {
    var user = db.users.find {
        // a nice approach with infix notation for DSLs with "eq" expression
        it.username eq userId
    }

    if(user == null) {
        val newUser = User()
        newUser.userId = userId
        newUser.weatherActionId = weatherActionId
        newUser.lat = lat
        newUser.lng = lng
        newUser.notifyAtHour = notifyAtHour
        newUser.lastNotified = null
        // if is an expression in kotlin
        newUser.createdOn = if(createdOn === null) Instant.now() else createdOn
        db.users.add(newUser)
        user = newUser
    } else {
        if(weatherActionId !== null) user.weatherActionId = weatherActionId
        if(lat !== null) user.lat = lat
        if(lng !== null) user.lng = lng
        if(notifyAtHour !== null) user.notifyAtHour = notifyAtHour
        if(lastNotified !== null) user.lastNotified = lastNotified
        user.flushChanges()
    }

    return user
}