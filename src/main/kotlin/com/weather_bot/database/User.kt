package com.weather_bot.database

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.math.BigDecimal
import java.time.Instant

interface User : Entity<User> {
    companion object : Entity.Factory<User>()
    val id: Int
    var userId: String
    var weatherActionId: Int?
    var lat: BigDecimal?
    var lng: BigDecimal?
    var notifyAtHour: Int?
    var lastNotified: Instant?
    var createdOn: Instant
}

object Users : Table<User>("user") {
    val id = int("id").primaryKey().bindTo { it.id }
    val username = varchar("username").bindTo { it.userId }
    val weatherActionId = int("weather_action_id").bindTo { it.weatherActionId }
    val lat = decimal("lat").bindTo { it.lat }
    val lng = decimal("lng").bindTo { it.lng }
    val notifyAtHour = int("notify_at_hour").bindTo { it.notifyAtHour }
    val lastNotified = timestamp("last_notified").bindTo { it.lastNotified }
    val createdOn = timestamp("created_on").bindTo { it.createdOn }
}

val Database.users get() = this.sequenceOf(Users)