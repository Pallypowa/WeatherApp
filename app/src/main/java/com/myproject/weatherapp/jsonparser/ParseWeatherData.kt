package com.myproject.weatherapp.jsonparser

import android.os.AsyncTask
import android.util.Log
import com.myproject.weatherapp.apihandler.CityDatas
import com.myproject.weatherapp.apihandler.MainTempData
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception

class ParseWeatherData(private val listener: OnDataAvailable) : AsyncTask<String, Void, CityDatas>() {
    private val TAG = "ParseWeatherData"

    interface OnDataAvailable {
        fun onDataAvailable(data: CityDatas)
        fun onError(exception: Exception)
    }
    override fun doInBackground(vararg params: String?): CityDatas {
        Log.d(TAG, "doInBackground: starts")
        var cityDatas = CityDatas()
        try {
            val jsonData = JSONObject(params[0])
            val listArray = jsonData.getJSONArray("list")
            cityDatas.cityName = jsonData.getJSONObject("city").getString("name")
            for(i in 0 until listArray.length()) {

                // "i. element from the jsonArray" Object from Json
                val jsonData = listArray.getJSONObject(i)

                // "main" Object from Json
                val mainJson = listArray.getJSONObject(i).getJSONObject("main")
                val temp: Float = mainJson.getString("temp").toFloat()
                val humidity: Int = mainJson.getString("humidity").toInt()
                val feelsLike: Float = mainJson.getString("feels_like").toFloat()

                // "weather" Object from Json
                val weatherJsonArray = listArray.getJSONObject(i).getJSONArray("weather")
                val weatherJson = weatherJsonArray.getJSONObject(0)
                val weatherType: String = weatherJson.getString("main")
                val description: String = weatherJson.getString("description")

                // "wind" Object from Json
                val windJson = listArray.getJSONObject(i).getJSONObject("wind")
                val windSpeed: Float = windJson.getString("speed").toFloat()

                val tempData = MainTempData(temp,feelsLike, humidity, weatherType, description, windSpeed)
                Log.d(TAG, "doInBackground: data $tempData")
                cityDatas.addWeatherData(tempData)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.d(TAG, "doInBackground: Error processing Json data. ${e.message}")
            cancel(true)
            listener.onError(e)
        }

        Log.d(TAG, "doInBackground: ends")
        return cityDatas
    }

    override fun onPostExecute(result: CityDatas) {
        super.onPostExecute(result)
        listener.onDataAvailable(result)
    }
}