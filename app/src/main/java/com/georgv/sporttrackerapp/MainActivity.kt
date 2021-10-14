package com.georgv.sporttrackerapp

import android.R.attr
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.replace
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.R.attr.tag
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import com.georgv.sporttrackerapp.database.SessionDB
import com.georgv.sporttrackerapp.database.SessionDao
import com.georgv.sporttrackerapp.viewmodel.SessionViewModel
import com.georgv.sporttrackerapp.viewmodel.TrackedSessionLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(),HistoryFragment.SendId,TrackingSessionFragment.UserWeightReceiver {
    private val smv:SessionViewModel by viewModels()

    private lateinit var trackedSessionLiveData: TrackedSessionLiveData
    private var userWeight:Double = 1.0


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

    suspend fun createTracker(){
        val db:SessionDB = SessionDB.get(applicationContext)
        val id = GlobalScope.async { db.sessionDao().getRunningSession(true).id }
        trackedSessionLiveData = TrackedSessionLiveData(this,id.await(),userWeight)
        trackedSessionLiveData.startLocationUpdates()
    }

    fun stopTracker(){
        if(trackedSessionLiveData != null){
            trackedSessionLiveData.stopLocationUpdates()
        }
    }

    override fun sendId(id:Long) {
        val f = navigateToDetailView()
        f.getSessionID(id)
    }

    override fun onPause() {
        super.onPause()

    }

    override fun getWeight(weight: Double) {
        userWeight = weight
    }
}