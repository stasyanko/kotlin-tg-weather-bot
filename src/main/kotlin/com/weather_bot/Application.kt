package com.weather_bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.SendMessage
import io.github.cdimascio.dotenv.Dotenv
import java.util.*

typealias chatId = Long
typealias stepNumber = Int

enum class StepEnum(val value: stepNumber) {
    WEATHER_ACTIONS(1),
    LOCATION(2),
    TIME(3),
}

fun main() {
    val dotenv = Dotenv.load()
    val bot = TelegramBot(dotenv["TG_BOT_TOKEN"])

    val chatSteps = Collections.synchronizedMap(mutableMapOf<
        chatId,
        StepEnum
    >())

    bot.setUpdatesListener { updates: List<Update?>? ->
        updates?.forEach { it ->
            //without null checks it does not compile
            val chatId: Long? = it?.message()?.chat()?.id()
            //let expr allows to do this without if/else
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
                //TODO: uncomment this to compile successfully
                StepEnum.TIME -> {
                    println("TIME")
                }
                null -> {

                }
            }
        }

        UpdatesListener.CONFIRMED_UPDATES_ALL
    }
}
