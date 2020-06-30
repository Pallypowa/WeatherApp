package com.myproject.weatherapp.apihandler

import android.os.AsyncTask
import java.lang.Exception
import java.net.URL


enum class DownloadStatus {
    IDLE, OK, ERROR
}
class GetWeatherData(private val listener:OnDownloadComplete) : AsyncTask<String, Void, String>() {
    private val TAG = "GetWeatherData"
    private var status: DownloadStatus = DownloadStatus.IDLE

    interface OnDownloadComplete {
        fun onDownloadComplete(result: String, status: DownloadStatus)
    }

    override fun doInBackground(vararg params: String?): String {
        if( params[0] == null ){
            status = DownloadStatus.ERROR
            return "No URL specified"
        }
        return try {
            status = DownloadStatus.OK
            URL(params[0]).readText()
        } catch (e: Exception) {
            status = DownloadStatus.ERROR
            "doInBackground: Invalid URL ${e.message}"
        }
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        //Log.d(TAG, "onPostExecute: $result")
        listener.onDownloadComplete(result, status)
    }
}