package com.myproject.weatherapp.activities

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.myproject.weatherapp.HumidityFragment
import com.myproject.weatherapp.TemperatureFragment
import com.myproject.weatherapp.WindSpeedFragment

@Suppress("DEPRECATION")
class ChartAdapter(context: Context, fm: FragmentManager, var tabCount: Int) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> TemperatureFragment()
            1 -> WindSpeedFragment()
            2 -> HumidityFragment()
            else -> getItem(position)
        }
    }

    override fun getCount(): Int = tabCount
}
