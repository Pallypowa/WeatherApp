package com.myproject.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.*
import com.myproject.weatherapp.apihandler.CityDatas
import com.myproject.weatherapp.apihandler.DownloadStatus
import com.myproject.weatherapp.apihandler.GetWeatherData
import com.myproject.weatherapp.apihandler.MainTempData
import com.myproject.weatherapp.jsonparser.ParseWeatherData
import com.myproject.weatherapp.locationhandler.GeocodingApi
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.weather_by_days.*
import org.json.JSONObject
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), GetWeatherData.OnDownloadComplete,
    ParseWeatherData.OnDataAvailable,
    GeocodingApi.OnLocationRequestComplete {
    private val context = this

    private val TAG = "MainActivity"
    private val CELSIUS = " °C"
    private val DESC_TEXT = "The high today will be %d$CELSIUS with winds at "

    private var downloader: GetWeatherData? = null

    private var apiCachedUrl = ""
    private var apiUrl =
        "https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=23c0b25438e4564dc253352dff80e09c&units=metric" // Default is Budapest
    private var iconUrl = "https://openweathermap.org/img/wn/%s@2x.png"
    private var cName: String? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest

    // Unique int
    private var PERMISSION_ID = 1000

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val networkConnection = NetworkConnection(applicationContext)
        networkConnection.observe(this, Observer { isConnected ->
            if (isConnected) {
                layoutDisconnected.visibility = View.GONE
                mainCardView.visibility = View.VISIBLE
                thisWeek.visibility = View.VISIBLE
                foreCastText.visibility = View.VISIBLE
                daysWidget.visibility = View.VISIBLE
                poweredBy.visibility = View.VISIBLE

                if (degreesCelsius.text == "") getLastLocation()

            } else {
                layoutDisconnected.visibility = View.VISIBLE
                mainCardView.visibility = View.GONE
                thisWeek.visibility = View.GONE
                foreCastText.visibility = View.GONE
                daysWidget.visibility = View.GONE
                poweredBy.visibility = View.GONE
            }
        })

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        // mainCardView dropdown menu
        mainCardView.setOnClickListener {
            Log.d(TAG, "onCreate mainCardView.setOnClickListener called")
            if (mainDropDown.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(mainCardView, AutoTransition())
                mainDropDown.visibility = View.VISIBLE
                dropDownArrow.setImageResource(R.drawable.dropdown_up_24)
            } else {
                TransitionManager.beginDelayedTransition(mainCardView, AutoTransition())
                mainDropDown.visibility = View.GONE
                TransitionManager.beginDelayedTransition(daysWidget as ViewGroup, AutoTransition())
                TransitionManager.endTransitions(mainCardView)
                dropDownArrow.setImageResource(R.drawable.dropdown_down_24)
            }
            Log.d(TAG, "onCreate mainCardView.setOnClickListener ends")
        }

        //downloadUrl(apiUrl)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        //for debugging
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Location permission granted")
                getLastLocation()
            }
        }
    }

    //Check user permission
    private fun checkPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    //Get user permission if needed
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    //Check if the location service of the device is enabled
    private fun isLocationEnabled(): Boolean {

        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun isOnline(): Boolean {
        var isConnected = false
        val connectivityMgr =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val allNetworks: Array<Network> =
            connectivityMgr.getAllNetworks() // added in API 21 (Lollipop)
        for (network in allNetworks) {
            val networkCapabilities: NetworkCapabilities =
                connectivityMgr.getNetworkCapabilities(network)
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            ) isConnected = true
        }
        return isConnected
    }

    //Get the last location
    private fun getLastLocation() {
        //check the permissions
        if (checkPermission()) {
            //check that the location service is enabled
            if (isLocationEnabled()) {
                //Getting the location
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    var location = task.result
                    if (location == null) {
                        getNewLocation()
                    } else {
                        var downloadLocation = GeocodingApi(this)
                        downloadLocation.execute(location)
                    }
                }
            } else {
                Toast.makeText(this, "Please enable location!", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermission()
        }
    }

    private fun getListener() = this

    //New Location (if location is null)
    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation = p0.lastLocation
            //new location
            var downloadLocation = GeocodingApi(getListener())
            downloadLocation.execute(lastLocation)
        }
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
        Log.d(TAG, "downloadUrl starting AsyncTask")

        downloader = GetWeatherData(this)
        downloader?.execute(apiUrl.format(cName))
        apiCachedUrl = apiUrl
        Log.d(TAG, "downloadUrl done")
    }

    override fun onDownloadComplete(result: String, status: DownloadStatus, type: String) {
        if (status == DownloadStatus.OK) {
            if (type == "WeatherData") {
                var parseWeatherData = ParseWeatherData(this)
                parseWeatherData.execute(result)
            } else {
                val jsonData = JSONObject(result)
                val jsonObject = jsonData.getJSONObject("address")
                Log.d(TAG, "onDownloadComplete: ${jsonObject.getString("city")}")
                cName = jsonObject.getString("city")
                downloadUrl(apiUrl)
            }
            //Log.d(TAG, "onDownloadComplete: $result")
        } else {
            //Log.e(TAG, "onDownloadComplete: $result")
        }
    }

    override fun onLocationRequestComplete(result: String) {
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
                //Log.d(TAG, "getKeys: ${data.time.dayOfWeek.toString()}")
                keys.add(data.time.dayOfWeek.toString())
            }
        }
        return keys
    }

    // 0 - calc temperature average
    // anything else -- calc wind average (I used -1)
    private fun calcAverage(list: List<MainTempData>, flag: Int): Double {
        var average = 0.0
        if (flag == 0) {
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


    private fun mostFrequentIcon(datas: List<MainTempData>): String? {
        var counter = HashMap<String, Int?>()

        for (data in datas) {
            if (!counter.containsKey(data.icon)) {
                counter[data.icon] = 1
            } else {
                val value = counter[data.icon]
                counter[data.icon] = value!! + 1
            }
        }
        var max = counter.maxWith(Comparator { a, b -> b.value?.let { a.value?.compareTo(it) }!! })
        //Log.d(TAG, "mostFrequentIcon: $max")
        return max?.key
    }


    private fun loadImagesToDays(
        keys: List<String>,
        datas: HashMap<String, List<MainTempData>>
    ): Unit {
        //today
        Picasso.get().load(iconUrl.format(mostFrequentIcon(datas[keys[0]]!!)))
            .error(R.drawable.temperature).placeholder(R.drawable.loading).into(imageView1)

        //tomorrow
        Picasso.get().load(iconUrl.format(mostFrequentIcon(datas[keys[1]]!!)))
            .error(R.drawable.temperature).placeholder(R.drawable.loading).into(imageView2)

        //etc...
        Picasso.get().load(iconUrl.format(mostFrequentIcon(datas[keys[2]]!!)))
            .error(R.drawable.temperature).placeholder(R.drawable.loading).into(imageView3)

        Picasso.get().load(iconUrl.format(mostFrequentIcon(datas[keys[3]]!!)))
            .error(R.drawable.temperature).placeholder(R.drawable.loading).into(imageView4)

        Picasso.get().load(iconUrl.format(mostFrequentIcon(datas[keys[4]]!!)))
            .error(R.drawable.temperature).placeholder(R.drawable.loading).into(imageView5)

        return
    }

    // Loads the data to the CardViews
    private fun loadDataToDays(keys: List<String>, datas: HashMap<String, List<MainTempData>>) {
        day1ID.text = keys.get(0).toLowerCase().subSequence(0, 3)
        day2ID.text = keys.get(1).toLowerCase().subSequence(0, 3)
        day3ID.text = keys.get(2).toLowerCase().subSequence(0, 3)
        day4ID.text = keys.get(3).toLowerCase().subSequence(0, 3)
        day5ID.text = keys.get(4).toLowerCase().subSequence(0, 3)

        temperatureDay1.text = calcAverage(datas[keys[0]]!!, 0).toInt().toString() + CELSIUS
        temperatureDay2.text = calcAverage(datas[keys[1]]!!, 0).toInt().toString() + CELSIUS
        temperatureDay3.text = calcAverage(datas[keys[2]]!!, 0).toInt().toString() + CELSIUS
        temperatureDay4.text = calcAverage(datas[keys[3]]!!, 0).toInt().toString() + CELSIUS
        temperatureDay5.text = calcAverage(datas[keys[4]]!!, 0).toInt().toString() + CELSIUS
        loadImagesToDays(keys, datas)

        //Log.d(TAG, "loadDataToDays: $}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDataAvailable(data: CityDatas) {
        dayDate.text =
            (data.getWeatherData(0).time.format(DateTimeFormatter.ofPattern("MMM dd yyyy")))
        cityName.text = data.cityName
        weatherMain.text = data.getWeatherData(0).description
        degreesCelsius.text = (data.getWeatherData(0).temp).roundToInt().toString() + " °C"

        // Dropdown details
        textPressure.text = (data.getWeatherData(0).pressure).toString() + " Pa"
        textHumidity.text = (data.getWeatherData(0).humidity).toString() + " %"
        textSomething1.text = (data.getWeatherData(0).windSpeed).toString() + " KPH"
        textSomething2.text = (data.getWeatherData(0).feelsLike).roundToInt().toString() + CELSIUS
        val dailyData = sortByDays(data)
        //Log.d(TAG, "onDataAvailable: ${data.getAllDAta()}")
        //Log.d(TAG, "onDataAvailable: ${sortedData["THURSDAY"]}")
        var keys = getKeys(data.getAllDAta())
        weatherDescription.text = DESC_TEXT.format(data.getWeatherData(0).tempMax.roundToInt()) +
                "${calcAverage(dailyData[keys[0]]!!, -1).toBigDecimal()
                    .setScale(2, RoundingMode.HALF_EVEN)} KPH"

        Picasso.get().load(iconUrl.format(data.getWeatherData(0).icon))
            .error(R.drawable.temperature).placeholder(R.drawable.loading).into(todayImageView)
        loadDataToDays(keys, dailyData)
    }

    override fun onError(exception: Exception) {
        Log.e(TAG, "onError: ${exception.message}")
    }
}