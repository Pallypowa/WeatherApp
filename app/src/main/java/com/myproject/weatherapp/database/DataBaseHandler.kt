package com.myproject.weatherapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.util.*


val DATABASE_NAME = "MyDb1"
val WEATHER_TABLE = "Weather_Data"
val COL_TEMP = "Temp"
val COL_DAY = "Day"
val COL_ID = "Id"
val COL_WIND = "WindSpeed"
val COL_HUMIDITY = "Humidity"

class DataBaseHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    val asd = this.writableDatabase
    private val TAG = "DataBaseHandler"
    override fun onCreate(database: SQLiteDatabase?) {
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
    fun insertData(todayData: TodayData) { //Akkor hívjuk meg, ha még nincs 14 rekord az adatbázisban, ilyenkor csak egyszerűen beszúrjuk az új értéket
        val db = this.writableDatabase
        if(isAlreadyInDatabase(todayData)) {
            val cv = ContentValues()
            cv.put(COL_WIND, todayData.windSpeed)
            cv.put(COL_TEMP, todayData.temp)
            cv.put(COL_HUMIDITY, todayData.humidity)
            cv.put(COL_DAY, todayData.time.dayOfMonth)
            val result = db.insert(WEATHER_TABLE, null, cv)
            if (result == -1.toLong()) Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
            else Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTodayData(todayData: TodayData){
        val dayToOverwrite = todayData.time.dayOfMonth.minus(14) //megkeressük a 14 nappal korábbi dátumot
        val db = this.readableDatabase
        val query = "SELECT * FROM $WEATHER_TABLE"
        val result = db.rawQuery(query, null)
        if( readMaxId() == 14 && isAlreadyInDatabase(todayData)){ //ha már szerepel 14 érték az adatbázisban és a mai még nem került bele, akkor a legrégebbit írja felül
            if(result.moveToFirst()){
                do {
                    val getDay =result.getString(result.getColumnIndex(COL_DAY)).toInt()
                    if (dayToOverwrite == getDay) {
                        val query = "UPDATE " + WEATHER_TABLE + " SET " +
                                COL_TEMP + "= " + todayData.temp + ", " +
                                COL_WIND + "= " + todayData.windSpeed + ", " +
                                COL_HUMIDITY + "= " + todayData.humidity + ", " +
                                COL_DAY + "= " + todayData.time.dayOfMonth +
                                " WHERE " + COL_DAY + "= " + dayToOverwrite + ";"

                        db.execSQL(query)
                        break;
                    } else {
                        result.moveToNext()
                    }
                }while(result.getString(result.getColumnIndex(COL_ID)).toInt()<14) //addig megy a ciklus, amíg el nem érünk a 14. naphoz
            }
        }else{
            insertData(todayData)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isAlreadyInDatabase(todayData: TodayData) : Boolean{ //ha az adott dátumhoz már van bent rekord, hamissal tér vissza
        val db = this.readableDatabase
        val query = "SELECT * FROM $WEATHER_TABLE"
        val result = db.rawQuery(query,null)
        if(result.moveToFirst()){
            do {
                if (todayData.time.dayOfMonth == result.getInt(result.getColumnIndex(COL_DAY))) {
                    return false
                } else {
                    result.moveToNext()
                }
            }while(result.getInt(result.getColumnIndex(COL_ID))<14) //addig megy a ciklus, amíg el nem érünk a 14. naphoz
        }
        return true;
    }

    fun readData() : MutableList<ChartData> { //visszaadja a tábla összes rekordját listában tárolva
        var dataList : MutableList<ChartData> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM $WEATHER_TABLE"
        val result = db.rawQuery(query, null)
        if(result.moveToFirst()){
            do{
                var temp = result.getFloat(result.getColumnIndex(COL_TEMP))
                var humidity = result.getInt(result.getColumnIndex(COL_HUMIDITY))
                var windSpeed = result.getFloat(result.getColumnIndex(COL_WIND))
                var day = result.getInt(result.getColumnIndex(COL_DAY))
                var chartData = ChartData(temp,humidity,windSpeed,day)
                dataList.add(chartData)
            }while(result.moveToNext())
        }
        return dataList
    }

    fun readMaxId() : Int{ //visszatér az adatbázisban szereplő legmagasabb id-val
        val db = this.readableDatabase
        val query = "SELECT * FROM $WEATHER_TABLE;"
        val result = db.rawQuery(query,null)
        result.moveToLast()
        return result.getInt(result.getColumnIndex(COL_ID))
    }
}