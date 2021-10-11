package com.georgv.sporttrackerapp.viewmodel

import SessionRepository
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.georgv.sporttrackerapp.customHandlers.CalorieCounter
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.data.TrackedSession
import com.georgv.sporttrackerapp.database.SessionDB
import java.util.*
import kotlinx.coroutines.*






class SessionViewModel(application: Application) : AndroidViewModel(application),SensorEventListener {

    private var db: SessionDB = SessionDB.get(getApplication())
    private var repo: SessionRepository = SessionRepository(application)
    private var runningSessionId:Long = 0
    val sessions: LiveData<List<Session>>
        get() = repo.getData()

    private var locationData = TrackedSessionLiveData(application)
    fun getData() = locationData

    fun getDistance() = locationData.getDistance()

    private var _steps = MutableLiveData<Long>(0)
    fun steps():LiveData<Long> = _steps

    private var _callories = MutableLiveData<Double>()
    fun callories():LiveData<Double> = _callories


    fun startSession(){
        val timestamp = TypeConverterUtil().dateToTimestamp(Date())
        val thisSession = Session(0,timestamp,true,null,null,null,null,null)
        GlobalScope.launch {
            runningSessionId = db.sessionDao().insert(thisSession)
            locationData.getSessionId(runningSessionId, getData())
        }
        startStepCounter(getApplication())
        locationData.startLocationUpdates()
    }

    fun stopSession(){
        val timestamp: Long = TypeConverterUtil().dateToTimestamp(Date())
        locationData.stopLocationUpdates()
        GlobalScope.launch {
            getDistance().value?.let { _steps.value?.let { it1 ->
                db.sessionDao().update(false,timestamp, it, 100f,
                    it1,100, runningSessionId)
            } }
            _steps.postValue(0)
            _callories.postValue(0.0)

        }


    }

    private fun startStepCounter(context: Context){
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepSensor == null) {
            Toast.makeText(context,"NO SENSOR ON THE DEVICE", Toast.LENGTH_SHORT)
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
        _steps.value?.let { a ->
            _steps.value = a + 1

        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }


    interface SessionIdGetter{
        fun getSessionId(id:Long,getter: SessionIdGetter)
    }




}