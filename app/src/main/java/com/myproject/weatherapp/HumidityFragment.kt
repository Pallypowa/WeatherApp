package com.myproject.weatherapp

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.myproject.weatherapp.database.ChartData
import com.myproject.weatherapp.database.DataBaseHandler
import kotlinx.android.synthetic.main.fragment_humidity.*
import kotlinx.android.synthetic.main.fragment_temperature.*

class HumidityFragment : Fragment() {
    lateinit var database: DataBaseHandler
    lateinit var chartDatas: MutableList<ChartData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = DataBaseHandler(this.activity!!.applicationContext)
        chartDatas = database.readData()
    }

    override fun onStart() {
        super.onStart()
        val entries = ArrayList<Entry>()
        for (i in 0 until chartDatas.size) {
            entries.add(Entry(chartDatas[i].day.toFloat(), chartDatas[i].humidity.toFloat()))
        }

        val line1 = LineDataSet(entries, "Humidity")
        line1.color = Color.CYAN
        line1.setDrawValues(false)

        line1.lineWidth = 3f
        line1.fillColor = Color.CYAN
        lineChartHumidity.xAxis.labelRotationAngle = 0f
        lineChartHumidity.axisRight.isEnabled = true
        lineChartHumidity.setTouchEnabled(true)
        lineChartHumidity.setPinchZoom(true)
        line1.axisDependency = YAxis.AxisDependency.LEFT
        lineChartHumidity.xAxis.textSize = 5f
        lineChartHumidity.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChartHumidity.xAxis.textSize = 5f
        lineChartHumidity.axisLeft.mAxisMinimum = 50f
        lineChartHumidity.axisLeft.mAxisMaximum = -20f
        lineChartHumidity.axisLeft.mAxisRange = 70f
        lineChartHumidity.description.text = "Humidity in the last 14 days"
        lineChartHumidity.data = LineData(line1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_humidity, container, false)
    }
}