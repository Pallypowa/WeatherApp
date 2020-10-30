package com.myproject.weatherapp


import android.graphics.Color
import android.os.Bundle
import android.util.Log
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


class TemperatureFragment : Fragment() {
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
            entries.add(Entry(chartDatas[i].day.toFloat(), chartDatas[i].temp))
        }

        val line1 = LineDataSet(entries, "Temperature")
        line1.color = Color.RED
        line1.setDrawValues(false)
        line1.setDrawFilled(true)
        line1.lineWidth = 3f
        line1.fillColor = Color.RED
        lineChart.xAxis.labelRotationAngle = 0f
        lineChart.axisRight.isEnabled = true
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        line1.axisDependency = YAxis.AxisDependency.LEFT
        lineChart.xAxis.textSize = 5f
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.textSize = 5f
        lineChart.axisLeft.mAxisMinimum = 50f
        lineChart.axisLeft.mAxisMaximum = -20f
        lineChart.axisLeft.mAxisRange = 70f
        lineChart.description.text = "Temperature in the last 14 days"
        lineChart.data = LineData(line1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_temperature, container, false)
    }

}