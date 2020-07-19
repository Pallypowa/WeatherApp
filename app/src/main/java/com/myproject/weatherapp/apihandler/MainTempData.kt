package com.myproject.weatherapp.apihandler

import java.time.LocalDateTime

data class MainTempData(
    val temp: Float,
    val tempMax: Float,
    val feelsLike: Float,
    val humidity: Int,
    val weatherType: String,
    val description: String,
    val windSpeed: Float,
    val time: LocalDateTime,
    val icon: String,
    val pressure: Int
)