package com.weather_bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.weather_bot.database.PgDatabase
import io.github.cdimascio.dotenv.Dotenv
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
    val bot = TelegramBot(dotenv["TG_BOT_TOKEN"])
    val chatSteps = Collections.synchronizedMap(mutableMapOf<chatId, StepEnum>())

    val pgDatabase = PgDatabase()
    val db = pgDatabase.connect(dotenv["DB_USER"], dotenv["DB_PASS"])
    pgDatabase.execSqlScript(INIT_SCRIPT, db)

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
