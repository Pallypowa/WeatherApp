package com.myproject.weatherapp.apihandler

import java.sql.Time

data class MainTempData(val temp: Float, val humidity: Int, val weatherType: String,
    val description: String, val windSpeed: Float) //TODO : Add val time: Time later