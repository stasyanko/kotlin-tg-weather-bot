package com.example.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*

fun Application.configureRouting() {

    routing {
        post ("/tg") {
            val formParameters = call.receiveParameters()
            val chatId = formParameters["chatId"].toString()
            val msg = formParameters["message.text"].toString()
            println(msg)
        }
    }
}
