package com.weather_bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.SendMessage
import io.github.cdimascio.dotenv.Dotenv

fun main() {
    val dotenv = Dotenv.load()
    val bot = TelegramBot(dotenv["TG_BOT_TOKEN"])

    bot.setUpdatesListener { updates: List<Update?>? ->
        updates?.forEach {
            val chatId: Long? = it?.message()?.chat()?.id()
            bot.execute(SendMessage(chatId, "Hello!"))
        }

        UpdatesListener.CONFIRMED_UPDATES_ALL
    }
}
