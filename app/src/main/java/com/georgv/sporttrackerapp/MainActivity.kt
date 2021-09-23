package com.georgv.sporttrackerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.replace
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.tracker -> {
                    // Respond to navigation item 1 click
                    navigateToTracker()
                    true
                }
                R.id.statistics -> {
                    // Respond to navigation item 2 click
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
        transaction.replace<TrackerFragment>(R.id.fragmentContainer)
        transaction.commit()
    }

    private fun navigateToStatistics() {
        Log.d("MainActivity.kt","BottomNavigation statistics clicked")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace<StatisticsFragment>(R.id.fragmentContainer)
        transaction.commit()
    }
}