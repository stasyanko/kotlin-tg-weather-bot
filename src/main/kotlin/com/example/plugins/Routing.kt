package com.example.plugins

import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.json.JSONObject

fun Application.configureRouting(dotenv: Dotenv) {
    val client = HttpClient(CIO)

    routing {
        post ("/") {
            try {
                val jsonData = JSONObject(call.receiveText())
                val message = jsonData.get("message") as JSONObject
                val chat = message.get("chat") as JSONObject

                val res = JSONObject()
                res.put("chat_id", chat.get("id").toString())
                res.put("text", "Hello!")

                client.request(dotenv.get("TG_BOT_BASE_URL") + "sendMessage") {
                    method = HttpMethod.Post
                    expectSuccess=true
                    contentType(ContentType.Application.Json)
                    setBody(res.toString())
                }
                call.respondText("")
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }
}
