package com.myproject.weatherapp.locationhandler

import android.location.Address
import android.location.Location
import android.os.AsyncTask
import android.util.Log
import com.myproject.weatherapp.apihandler.DownloadStatus
import com.myproject.weatherapp.apihandler.GetWeatherData
import java.lang.Exception
import java.net.URL

enum class DownloadStatus {
    IDLE, OK, ERROR
}

class GeocodingApi(private val listener: GetWeatherData.OnDownloadComplete) :
    AsyncTask<Location, Void, String>() {
    private val TAG = "GeocodingApi"
    private var status: DownloadStatus = DownloadStatus.IDLE
    private var apiUrl =
        "https://eu1.locationiq.com/v1/reverse.php?key=2ed51fc7f9f95a&lat=%f&lon=%f&format=json"

    interface OnLocationRequestComplete {
        fun onLocationRequestComplete(results: String)
    }

    override fun doInBackground(vararg params: Location?): String {
        if (params[0] == null) {
            status = DownloadStatus.ERROR
            return "No URL specified"
        }
        return try {
            status = DownloadStatus.OK
            URL(apiUrl.format(params[0]?.latitude, params[0]?.longitude)).readText()
        } catch (e: Exception) {
            status = DownloadStatus.ERROR
            "doInBackground: Invalid URL ${e.message}"
        }
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        Log.d(TAG, "onPostExecute: $result")
        listener.onDownloadComplete(result, status, "")
    }
}