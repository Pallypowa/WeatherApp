package com.myproject.weatherapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi


val DATABASE_NAME = "MyDb1"
val WEATHER_TABLE = "Weather_Data"
val WIND_TABLE = "Wind_Data"
val HUMIDITY_TABLE = "Humidity_Data"
val COL_TEMP = "Temp"
val COL_DAY = "Day"
val COL_ID = "Id"
val COL_WIND = "WindSpeed"
val COL_HUMIDITY = "Humidity"

class DataBaseHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    val asd = this.writableDatabase
    private val TAG = "DataBaseHandler"
    override fun onCreate(database: SQLiteDatabase?) {
        Log.d(TAG, "fasz")
        val createTempTable = "CREATE TABLE $WEATHER_TABLE " +
                "($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_TEMP FLOAT, " +
                "$COL_WIND FLOAT, " +
                "$COL_HUMIDITY INT, " +
                "$COL_DAY INT)"
        database?.execSQL(createTempTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun tableDrop() {
        val db = this.writableDatabase
        val dropDatble = "DROP TABLE $WEATHER_TABLE"
        db.execSQL(dropDatble)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(todayData: TodayData) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_WIND, todayData.windSpeed)
        cv.put(COL_TEMP, todayData.temp)
        cv.put(COL_HUMIDITY, todayData.humidity)
        cv.put(COL_DAY, todayData.time.dayOfMonth)
        val result = db.insert(WEATHER_TABLE, null, cv)
        if (result == -1.toLong()) Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
    }

    fun readData() {
        val db = this.readableDatabase
        val query = "SELECT * FROM WEATHER_TABLE"
        val result = db.rawQuery(query, null)
        val value = result.moveToFirst()
        Toast.makeText(context, result.getString(1), Toast.LENGTH_SHORT).show()
    }
}