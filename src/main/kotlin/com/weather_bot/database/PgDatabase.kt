package com.weather_bot.database

import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.support.postgresql.PostgreSqlDialect

class PgDatabase {
    fun connect(
        user: String,
        password: String
    ): Database {
        return Database.connect(
            user = user,
            password = password,
            url = "jdbc:postgresql:tg_weather_bot",
            dialect = PostgreSqlDialect(),
            logger = ConsoleLogger(threshold = LogLevel.INFO)
        )
    }

    fun execSqlScript(filename: String, database: Database) {
        database.useConnection { conn ->
            conn.createStatement().use { statement ->
                javaClass.classLoader
                    ?.getResourceAsStream(filename)
                    ?.bufferedReader()
                    ?.use { reader ->
                        for (sql in reader.readText().split(';')) {
                            if (sql.any { it.isLetterOrDigit() }) {
                                statement.executeUpdate(sql)
                            }
                        }
                    }
            }
        }
    }
}