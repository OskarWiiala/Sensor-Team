package com.georgv.sporttrackerapp.viewmodel

import SessionRepository
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.georgv.sporttrackerapp.TrackingSessionFragment
import com.georgv.sporttrackerapp.data.LocationPoint
import com.georgv.sporttrackerapp.database.SessionDB
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.*



class TrackedSessionLiveData(context: Context) : LiveData<LocationPoint>(),SessionViewModel.SessionIdGetter {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val db by lazy { SessionDB.get(context) }
    private var sessionId:Long = 0



    private var totalDistanceTraveled = MutableLiveData<Float>(0f)
    fun getDistance():LiveData<Float> = totalDistanceTraveled

    private var steps = MutableLiveData<Int>()
    fun getSteps():LiveData<Int> = steps


    companion object {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun setLocationPoint(locationPoint: Location): LocationPoint {

        val newLocPoint =
            LocationPoint(
                0,
                longtitude = locationPoint.longitude,
                latitude = locationPoint.latitude,
                sessionID = sessionId,
                currentSpeed = locationPoint.speed
            )
        value = newLocPoint
        return newLocPoint
    }


    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                val locPoint = setLocationPoint(location)

                GlobalScope.launch {
                    db.locationPointDao().insert(locPoint)
                    totalDistanceTraveled.postValue(countDistance().await())
                }




            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null,
        )
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        totalDistanceTraveled.postValue(0f)
        //value = LocationPoint(0,sessionId,0.0,0.0,0f)
    }


    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.also {
                    setLocationPoint(location)
                    Log.d("ON ACTIVE","ON ACTIVE")
                }
            }
        startLocationUpdates()
    }

    private fun countDistance() = GlobalScope.async {
            val results = FloatArray(1)
            val list = db.locationPointDao().getBySessionId(sessionId)
            if (list.count() > 1) {
                val lastPoint: LocationPoint = list.last()
                val prevPoint: LocationPoint = list[list.lastIndex - 1]
                Location.distanceBetween(
                    prevPoint.latitude,
                    prevPoint.longtitude,
                    lastPoint.latitude,
                    lastPoint.longtitude,
                    results
                )
            }
        val traveledDistance = totalDistanceTraveled.value?.plus(results[0])
            return@async traveledDistance
    }


    override fun getSessionId(id: Long, getter:SessionViewModel.SessionIdGetter) {
        sessionId = id
    }







}