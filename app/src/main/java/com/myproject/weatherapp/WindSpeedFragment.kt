package com.myproject.weatherapp

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.myproject.weatherapp.database.ChartData
import com.myproject.weatherapp.database.DataBaseHandler
import kotlinx.android.synthetic.main.fragment_temperature.*
import kotlinx.android.synthetic.main.fragment_wind_speed.*


class WindSpeedFragment : Fragment() {
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
            entries.add(Entry(chartDatas[i].day.toFloat(), chartDatas[i].windSpeed))
        }

        val line1 = LineDataSet(entries, "Wind speed")
        //line1.mode = LineDataSet.Mode.CUBIC_BEZIER
        line1.color = Color.BLUE
        line1.setDrawValues(false)
        line1.setDrawFilled(true)
        line1.lineWidth = 3f
        line1.fillColor = Color.BLUE
        lineChartWind.xAxis.labelRotationAngle = 0f
        lineChartWind.axisRight.isEnabled = true
        lineChartWind.setTouchEnabled(true)
        lineChartWind.setPinchZoom(true)
        line1.axisDependency = YAxis.AxisDependency.LEFT
        lineChartWind.xAxis.textSize = 5f
        lineChartWind.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChartWind.xAxis.textSize = 5f
        lineChartWind.axisLeft.mAxisMinimum = 50f
        lineChartWind.axisLeft.mAxisMaximum = -20f
        lineChartWind.axisLeft.mAxisRange = 70f
        lineChartWind.description.text = "WindSpeed in the last 14 days"
        lineChartWind.data = LineData(line1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wind_speed, container, false)
    }


}