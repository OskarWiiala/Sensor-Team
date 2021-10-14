package com.georgv.sporttrackerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.replace
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.georgv.sporttrackerapp.database.SessionDB
import com.georgv.sporttrackerapp.viewmodel.TrackedSessionLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class MainActivity : AppCompatActivity(),HistoryFragment.SendId,TrackingSessionFragment.UserWeightReceiver {
    //private val viewModel:SessionViewModel by viewModels()
    private val db by lazy { SessionDB.get(applicationContext) }
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
        val job = GlobalScope.async { db.sessionDao().getRunningSession(true) }
        val session = job.await()
        if(session != null) {
            trackedSessionLiveData = TrackedSessionLiveData(this, session.id, userWeight)
            trackedSessionLiveData.startLocationUpdates()
        }
    }

    fun stopTracker(){
        if(::trackedSessionLiveData.isInitialized){
            trackedSessionLiveData.stopLocationUpdates()
        }
    }

    fun keepTracking():Boolean{
        return !::trackedSessionLiveData.isInitialized
    }

    override fun sendId(id:Long) {
        val f = navigateToDetailView()
        f.getSessionID(id)
    }

    override fun getWeight(weight: Double) {
        userWeight = weight
    }
}