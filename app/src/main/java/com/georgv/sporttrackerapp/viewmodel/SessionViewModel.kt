package com.georgv.sporttrackerapp.viewmodel

import SessionRepository
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.georgv.sporttrackerapp.TrackingSessionFragment
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.LocationPoint
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.data.TrackedSession
import com.georgv.sporttrackerapp.database.SessionDB
import java.util.*
import kotlinx.coroutines.*
import org.osmdroid.util.GeoPoint


class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private var db: SessionDB = SessionDB.get(application)
    private var repo: SessionRepository = SessionRepository(application)

    val sessions: LiveData<List<Session>>
        get() = repo.getData()

    private var _session: LiveData<TrackedSession> = db.sessionDao().getRunningSessionAsLiveData(true)
    val session: LiveData<TrackedSession>
        get() = _session


    private var locationArray: MutableList<GeoPoint> = mutableListOf()
    fun getLocationArray(): List<GeoPoint> = locationArray

    fun addToLocationArray(location: GeoPoint) {
        locationArray.add(location)
    }

    private var userWeightValue:Double = 0.0



    private suspend fun storeToDatabase(): Long {
        val timestamp = TypeConverterUtil().dateToTimestamp(Date())
        val thisSession = Session(0, timestamp, true, null, 0f, 0f, 0, 0)
        val thisSessionId = db.sessionDao().insert(thisSession)
        return thisSessionId
    }

   private suspend fun updateToDatabase(){
       val timestamp: Long = TypeConverterUtil().dateToTimestamp(Date())
       val job = GlobalScope.async { db.sessionDao().getRunningSession(true).id }
       val id = job.await()
       db.sessionDao().finalSessionUpdate(false, timestamp,id)
   }


    fun startSession() {
        runBlocking {
            val createSession = GlobalScope.async { storeToDatabase() }
            createSession.await()
            _session
        }
    }

    fun stopSession() {
        GlobalScope.launch {
            updateToDatabase()
            _session
        }
    }


    interface SessionStateReciever{
        fun setRunning(state: Boolean):Boolean
    }
}