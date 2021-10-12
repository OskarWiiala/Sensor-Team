package com.georgv.sporttrackerapp.viewmodel

import SessionRepository
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.database.SessionDB
import java.util.*
import kotlinx.coroutines.*
import org.osmdroid.util.GeoPoint


class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private var db: SessionDB = SessionDB.get(getApplication())
    private var repo: SessionRepository = SessionRepository(application)
    private var runningSessionId: Long = 0

    val sessions: LiveData<List<Session>>
        get() = repo.getData()

    private var _session: LiveData<Session>? = null
    val session: LiveData<Session>?
        get() = _session

    private var locationData = TrackedSessionLiveData(application)
    fun getData() = locationData

    private var locationArray: MutableList<GeoPoint> = mutableListOf()
    fun getLocationArray(): List<GeoPoint> = locationArray

    fun addToLocationArray(location: GeoPoint) {
        locationArray.add(location)
    }


    fun startSession() {
        runBlocking {
            val createSession = GlobalScope.async { storeToDatabase() }
            runningSessionId = createSession.await()
            locationData.getSessionId(runningSessionId, getData())
            _session = repo.getSession(runningSessionId).asLiveData()
        }
        locationData.startLocationUpdates()
    }

    suspend fun storeToDatabase(): Long {
        val timestamp = TypeConverterUtil().dateToTimestamp(Date())
        val thisSession = Session(0, timestamp, true, null, null, null, null, null)
        val thisSessionId = db.sessionDao().insert(thisSession)
        return thisSessionId
    }


    fun stopSession() {
        GlobalScope.launch {
            val timestamp: Long = TypeConverterUtil().dateToTimestamp(Date())
            db.sessionDao().finalSessionUpdate(false, timestamp, runningSessionId)
            locationData.stopLocationUpdates()
            runningSessionId = 0
            _session = null
        }

    }


    interface SessionIdGetter {
        fun getSessionId(id: Long, getter: SessionIdGetter)
    }
}