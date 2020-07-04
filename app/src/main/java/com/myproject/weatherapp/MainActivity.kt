package com.myproject.weatherapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import com.myproject.weatherapp.apihandler.CityDatas
import com.myproject.weatherapp.apihandler.DownloadStatus
import com.myproject.weatherapp.apihandler.GetWeatherData
import com.myproject.weatherapp.apihandler.MainTempData
import com.myproject.weatherapp.jsonparser.ParseWeatherData
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.weather_by_days.*
import java.lang.Exception
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), GetWeatherData.OnDownloadComplete,
    ParseWeatherData.OnDataAvailable {
    private val TAG = "MainActivity"
    private val CELSIUS = " °C"
    private val DESC_TEXT = "The high today will be %d$CELSIUS with winds at "

    private var downloader: GetWeatherData? = null
    private var apiCachedUrl = ""
    private var apiUrl =
        "https://api.openweathermap.org/data/2.5/forecast?q=Budapest&appid=23c0b25438e4564dc253352dff80e09c&units=metric" // Default is Budapest
    private var iconUrl = "https://openweathermap.org/img/wn/%s@2x.png"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadUrl(apiUrl)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.cities_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        apiUrl = when (item.itemId) {
            R.id.budapest -> "https://api.openweathermap.org/data/2.5/forecast?q=Budapest&appid=23c0b25438e4564dc253352dff80e09c&units=metric"
            R.id.debrecen -> "https://api.openweathermap.org/data/2.5/forecast?q=Debrecen&appid=23c0b25438e4564dc253352dff80e09c&units=metric"
            R.id.veszprem -> "https://api.openweathermap.org/data/2.5/forecast?q=Veszprem&appid=23c0b25438e4564dc253352dff80e09c&units=metric"
            else -> return super.onOptionsItemSelected(item)
        }

        downloadUrl(apiUrl)
        return true
    }

    private fun downloadUrl(apiUrl: String) {
        if (apiUrl != apiCachedUrl) {
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
        if (status == DownloadStatus.OK) {
            var parseWeatherData = ParseWeatherData(this)
            parseWeatherData.execute(result)
            Log.d(TAG, "onDownloadComplete: $result")
        } else {
            Log.e(TAG, "onDownloadComplete: $result")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sortByDays(datas: CityDatas): HashMap<String, List<MainTempData>> {
        var dataByDays = HashMap<String, List<MainTempData>>()

        for (data in datas.getAllDAta()) {
            if (!dataByDays.containsKey(data.time.dayOfWeek.toString())) {
                var tempDataList = mutableListOf<MainTempData>()
                for (tempData in datas.getAllDAta()) {
                    if (data.time.dayOfWeek == tempData.time.dayOfWeek) tempDataList.add(tempData)
                }
                dataByDays[data.time.dayOfWeek.toString()] = tempDataList
            }
        }
        return dataByDays
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getKeys(list: List<MainTempData>): List<String> {
        var keys = mutableListOf<String>()
        for (data in list) {
            if (!keys.contains(data.time.dayOfWeek.toString())) {
                Log.d(TAG, "getKeys: ${data.time.dayOfWeek.toString()}")
                keys.add(data.time.dayOfWeek.toString())
            }
        }
        return keys
    }

    // 0 - calc temperature average
    // anything else -- calc wind average (I used -1)
    private fun calcAverage(list: List<MainTempData>, flag: Int): Double {
        var average = 0.0
        if(flag == 0){
            for (temp in list) {
                average += temp.temp
            }
        } else {
            for (temp in list) {
                average += temp.windSpeed
            }
        }

        return (average / list.size)
    }



    private fun mostFrequentIcon(datas: List<MainTempData>) : String? {
        var counter = HashMap<String, Int?>()

        for(data in datas) {
            if(!counter.containsKey(data.icon)){
                counter[data.icon] = 1
            } else {
                val value = counter[data.icon]
                counter[data.icon] = value!! + 1
            }
        }
        var max = counter.maxWith(Comparator { a, b -> b.value?.let { a.value?.compareTo(it) }!! })
        Log.d(TAG, "mostFrequentIcon: $max")
        return max?.key
    }


    private fun loadImagesToDays(keys: List<String>, datas: HashMap<String, List<MainTempData>>) : Unit {
        //today
        Picasso.get().load(iconUrl.format(mostFrequentIcon(datas[keys[0]]!!))).
        error(R.drawable.temperature).
        placeholder(R.drawable.loading).
        into(imageView1)

        //tomorrow
        Picasso.get().load(iconUrl.format(mostFrequentIcon(datas[keys[1]]!!))).
        error(R.drawable.temperature).
        placeholder(R.drawable.loading).
        into(imageView2)

        //etc...
        Picasso.get().load(iconUrl.format(mostFrequentIcon(datas[keys[2]]!!))).
        error(R.drawable.temperature).
        placeholder(R.drawable.loading).
        into(imageView3)

        Picasso.get().load(iconUrl.format(mostFrequentIcon(datas[keys[3]]!!))).
        error(R.drawable.temperature).
        placeholder(R.drawable.loading).
        into(imageView4)

        Picasso.get().load(iconUrl.format(mostFrequentIcon(datas[keys[4]]!!))).
        error(R.drawable.temperature).
        placeholder(R.drawable.loading).
        into(imageView5)

        return
    }
    // Loads the data to the CardViews
    private fun loadDataToDays(keys: List<String>, datas: HashMap<String, List<MainTempData>>) {
        day1ID.text = keys.get(0).toLowerCase().subSequence(0,3)
        day2ID.text = keys.get(1).toLowerCase().subSequence(0,3)
        day3ID.text = keys.get(2).toLowerCase().subSequence(0,3)
        day4ID.text = keys.get(3).toLowerCase().subSequence(0,3)
        day5ID.text = keys.get(4).toLowerCase().subSequence(0,3)

        temperatureDay1.text = calcAverage(datas[keys[0]]!!, 0).toInt().toString() + CELSIUS
        temperatureDay2.text = calcAverage(datas[keys[1]]!!, 0).toInt().toString() + CELSIUS
        temperatureDay3.text = calcAverage(datas[keys[2]]!!, 0).toInt().toString() + CELSIUS
        temperatureDay4.text = calcAverage(datas[keys[3]]!!, 0).toInt().toString() + CELSIUS
        temperatureDay5.text = calcAverage(datas[keys[4]]!!, 0).toInt().toString() + CELSIUS
        loadImagesToDays(keys, datas)

        Log.d(TAG, "loadDataToDays: $}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDataAvailable(data: CityDatas) {
        dayDate.text = (data.getWeatherData(0).time.format(DateTimeFormatter.ofPattern("MMM dd yyyy")))
        cityName.text = data.cityName
        weatherMain.text = data.getWeatherData(0).description
        degreesCelsius.text = (data.getWeatherData(0).temp).roundToInt().toString() + " °C"
        val dailyData = sortByDays(data)
        //Log.d(TAG, "onDataAvailable: ${data.getAllDAta()}")
        //Log.d(TAG, "onDataAvailable: ${sortedData["THURSDAY"]}")
        var keys = getKeys(data.getAllDAta())
        weatherDescription.text = DESC_TEXT.format(data.getWeatherData(0).tempMax.toInt()) +
                "${calcAverage(dailyData[keys[0]]!!, -1).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)} KPH"

        Picasso.get().load(iconUrl.format(data.getWeatherData(0).icon)).
        error(R.drawable.temperature).
        placeholder(R.drawable.loading).
        into(todayImageView)
        loadDataToDays(keys, dailyData)
    }

    override fun onError(exception: Exception) {
        Log.e(TAG, "onError: ${exception.message}")
    }
}