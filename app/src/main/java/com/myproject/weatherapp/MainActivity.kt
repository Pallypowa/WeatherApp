package com.myproject.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.myproject.weatherapp.apihandler.CityDatas
import com.myproject.weatherapp.apihandler.DownloadStatus
import com.myproject.weatherapp.apihandler.GetWeatherData
import com.myproject.weatherapp.jsonparser.ParseWeatherData
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), GetWeatherData.OnDownloadComplete, ParseWeatherData.OnDataAvailable {
    private val TAG = "MainActivity"

    private var downloader: GetWeatherData? = null
    private var apiCachedUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadUrl("https://api.openweathermap.org/data/2.5/forecast?q=Budapest&appid=23c0b25438e4564dc253352dff80e09c&units=metric")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.cities_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val apiUrl: String

        apiUrl = when (item.itemId) {
            R.id.budapest -> "https://api.openweathermap.org/data/2.5/forecast?q=Budapest&appid=23c0b25438e4564dc253352dff80e09c&units=metric"
            R.id.debrecen -> "https://api.openweathermap.org/data/2.5/forecast?q=Debrecen&appid=23c0b25438e4564dc253352dff80e09c&units=metric"
            R.id.veszprem -> "https://api.openweathermap.org/data/2.5/forecast?q=Veszprem&appid=23c0b25438e4564dc253352dff80e09c&units=metric"
            else -> return super.onOptionsItemSelected(item)
        }

        downloadUrl(apiUrl)
        return true
    }

    private fun downloadUrl(apiUrl: String){
        if (apiUrl != apiCachedUrl){
            Log.d(TAG, "downloadUrl starting AsyncTask")
            downloader = GetWeatherData(this)
            downloader?.execute(apiUrl)
            apiCachedUrl = apiUrl
            Log.d(TAG, "downloadUrl done")
        } else {
            Log.d(TAG, "downloadUrl - URL not changed")
        }
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
        degreesCelsius.text = (data.getWeatherData(0).temp).roundToInt().toString() + " °C"
        degreesFeelsLike.text = (data.getWeatherData(0).feelsLike).roundToInt().toString() + " °C"

    }

    override fun onError(exception: Exception) {
        Log.e(TAG, "onError: ${exception.message}")
    }
}