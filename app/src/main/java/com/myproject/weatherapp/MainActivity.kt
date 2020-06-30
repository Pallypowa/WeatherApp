package com.myproject.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.myproject.weatherapp.apihandler.CityDatas
import com.myproject.weatherapp.apihandler.DownloadStatus
import com.myproject.weatherapp.apihandler.GetWeatherData
import com.myproject.weatherapp.jsonparser.ParseWeatherData
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity(), GetWeatherData.OnDownloadComplete, ParseWeatherData.OnDataAvailable {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var downloader = GetWeatherData(this)
        downloader.execute("https://api.openweathermap.org/data/2.5/forecast?q=Budapest&appid=23c0b25438e4564dc253352dff80e09c&units=metric")
    }

    override fun onDownloadComplete(result: String, status: DownloadStatus) {
        if ( status == DownloadStatus.OK ) {
            var parseWeatherData: ParseWeatherData = ParseWeatherData(this)
            parseWeatherData.execute(result)
            Log.d(TAG, "onDownloadComplete: $result")
        } else {
            Log.e(TAG, "onDownloadComplete: $result")
        }
    }

    override fun onDataAvailable(data: CityDatas) {
        cityName.text = data.cityName
        weatherMain.text = data.getWeatherData(0).weatherType
        weatherDescription.text = data.getWeatherData(0).description
        degreesCelsius.text = (data.getWeatherData(0).temp).toInt().toString() + " °C"
    }

    override fun onError(exception: Exception) {
        Log.e(TAG, "onError: ${exception.message}")
    }
}