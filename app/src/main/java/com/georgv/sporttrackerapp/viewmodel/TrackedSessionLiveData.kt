package com.georgv.sporttrackerapp.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.georgv.sporttrackerapp.customHandlers.CalorieCounter
import com.georgv.sporttrackerapp.data.LocationPoint
import com.georgv.sporttrackerapp.database.SessionDB
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.*


class TrackedSessionLiveData(context: Context) : LiveData<LocationPoint>(),
    SessionViewModel.SessionIdGetter,
    SensorEventListener {

    private val context = context;
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val db by lazy { SessionDB.get(context) }
    private var sessionId: Long = 0

    private val speedList = mutableListOf<Float>()
    private var averageSpeed: Float = 0f
    private var totalDistanceTraveled = 0f

    private var steps: Long = 0


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
        speedList.add(newLocPoint.currentSpeed)
        averageSpeed = speedList.sum() / speedList.count()
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
                    totalDistanceTraveled = countDistance().await()
                    val caloiries = CalorieCounter.countCalories(totalDistanceTraveled, 50.5)
                    db.sessionDao().update(
                        true,
                        null,
                        totalDistanceTraveled,
                        averageSpeed,
                        steps,
                        caloiries,
                        sessionId
                    )
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
        startStepCounter()
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        totalDistanceTraveled = 0f
        averageSpeed = 0f
        speedList.clear()
        steps = 0
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
        val traveledDistance = totalDistanceTraveled.plus(results[0])
        return@async traveledDistance
    }


    private fun startStepCounter() {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepSensor == null) {
            Toast.makeText(context, "NO SENSOR ON THE DEVICE", Toast.LENGTH_SHORT)
                .show()
        } else {
            sensorManager.registerListener(
                this,
                stepSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        Log.d(p0.toString(), "EVENT")
        steps++
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun getSessionId(id: Long, getter: SessionViewModel.SessionIdGetter) {
        sessionId = id
    }


}