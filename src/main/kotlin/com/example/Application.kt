package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.github.cdimascio.dotenv.Dotenv

fun main() {
    val dotenv = Dotenv.load()

    embeddedServer(Netty, port = 8080, host = "localhost") {
        configureRouting(dotenv)
    }.start(wait = true)
}
