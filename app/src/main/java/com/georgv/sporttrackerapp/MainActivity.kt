package com.georgv.sporttrackerapp

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.replace
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.georgv.sporttrackerapp.database.SessionDB
import com.georgv.sporttrackerapp.viewmodel.SessionViewModel
import com.georgv.sporttrackerapp.viewmodel.TrackedSessionLiveData
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class MainActivity : AppCompatActivity(),HistoryFragment.SendId{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        TrackedSessionLiveData.fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
        TrackedSessionLiveData.sensorManager = application.getSystemService(
            Context.SENSOR_SERVICE) as SensorManager

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.tracker -> {
                    // Respond to navigation item 1 click
                    navigateToTracker()
                    true
                }
                R.id.history -> {
                    // Respond to navigation item 2 click
                    navigateToHistory()
                    true
                }
                R.id.statistics -> {
                    // Respond to navigation item 3 click
                    navigateToStatistics()
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToTracker() {
        Log.d("MainActivity.kt","BottomNavigation tracker clicked")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace<TrackingSessionFragment>(R.id.fragmentContainer)
        transaction.commit()
    }

    private fun navigateToStatistics() {
        Log.d("MainActivity.kt","BottomNavigation statistics clicked")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace<StatisticsFragment>(R.id.fragmentContainer)
        transaction.commit()
    }

    private fun navigateToHistory() {
        Log.d("MainActivity.kt","BottomNavigation history clicked")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace<HistoryFragment>(R.id.fragmentContainer)
        transaction.commit()
    }

    private fun navigateToDetailView():SessionDetailFragment{
        val fragment = SessionDetailFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace<SessionDetailFragment>(R.id.fragmentContainer)
        .replace(R.id.fragmentContainer,fragment).commit()
        return fragment
    }


    override fun sendId(id:Long) {
        val f = navigateToDetailView()
        f.getSessionID(id)
    }


}