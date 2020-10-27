package com.myproject.weatherapp.database

import java.time.LocalDateTime

data class TodayData(
    val temp: Float,
    val humidity: Int,
    val windSpeed: Float,
    val time: LocalDateTime
)