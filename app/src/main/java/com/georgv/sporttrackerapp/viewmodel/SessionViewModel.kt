package com.georgv.sporttrackerapp.viewmodel

import SessionRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.georgv.sporttrackerapp.HistoryAdapter
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.database.SessionDB
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SessionViewModel(application: Application) : AndroidViewModel(application){
    private val db: SessionDB = SessionDB.get(getApplication())
    private val repo: SessionRepository = SessionRepository(application)
    var chosenSession:Session? = null
    val sessions: LiveData<List<Session>>
        get() = repo.getData()


    fun insertTest() {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = Date()

        val session = Session(
            0,
            TypeConverterUtil().dateToTimestamp(currentDate),
            TypeConverterUtil().dateToTimestamp(currentDate),
            14.5, 15f, 25f, 14, 14
        )

        GlobalScope.launch { db.sessionDao().insert(session) }

    }



}