package com.myproject.weatherapp.apihandler

class CityDatas(var cityName: String = "Unknown") {
    private var weatherDatas = mutableListOf<MainTempData>()
    fun addWeatherData(data: MainTempData){
        weatherDatas.add(data)
    }
    fun getWeatherData(index: Int) = weatherDatas[index]
    fun getWeatherDataSize() = weatherDatas.size
    fun getAllDAta() = weatherDatas
}