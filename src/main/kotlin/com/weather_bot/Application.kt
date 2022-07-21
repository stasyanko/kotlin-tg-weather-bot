package com.weather_bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.weather_bot.database.PgDatabase
import com.weather_bot.database.User
import com.weather_bot.database.users
import io.github.cdimascio.dotenv.Dotenv
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import java.math.BigDecimal
import java.time.Instant
import java.util.*

typealias chatId = Long
typealias stepNumber = Int

enum class StepEnum(val value: stepNumber) {
    WEATHER_ACTIONS(1),
    LOCATION(2),
    TIME(3),
}

private const val INIT_SCRIPT = "database/init-pg-data.sql"

fun main() {
    val dotenv = Dotenv.load()

    val pgDatabase = PgDatabase()
    val db = pgDatabase.connect(dotenv["DB_USER"], dotenv["DB_PASS"])
    pgDatabase.execSqlScript(INIT_SCRIPT, db)

    val bot = TelegramBot(dotenv["TG_BOT_TOKEN"])
    val chatSteps = Collections.synchronizedMap(mutableMapOf<chatId, StepEnum>())

    bot.setUpdatesListener { updates: List<Update?>? ->
        updates?.forEach { it ->
            val chatId: Long? = it?.message()?.chat()?.id()
            val chatStep = chatSteps[chatId].let { stepNumber ->
                if(stepNumber === null) {
                    chatSteps[chatId] = StepEnum.WEATHER_ACTIONS
                }

                chatSteps[chatId]
            }

            when(chatStep) {
                StepEnum.WEATHER_ACTIONS -> {
                    println("WEATHER_ACTIONS")
                }
                StepEnum.LOCATION -> {
                    println("LOCATION")
                }
                StepEnum.TIME -> {
                    println("TIME")
                }
                null -> {
                    println("empty step")
                }
            }
        }

        UpdatesListener.CONFIRMED_UPDATES_ALL
    }
}

private fun upsertUser(
    db: Database,
    userId: String,
    weatherActionId: Int?,
    lat: BigDecimal?,
    lng: BigDecimal?,
    notifyAtHour: Int?,
    lastNotified: Instant?,
    createdOn: Instant?
): User {
    var user = db.users.find {
        // a nice approach for DSLs with "eq" expression
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