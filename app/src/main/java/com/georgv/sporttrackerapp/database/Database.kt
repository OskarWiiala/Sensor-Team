package com.georgv.sporttrackerapp.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.georgv.sporttrackerapp.data.LocationPoint
import com.georgv.sporttrackerapp.data.Session

@Database(entities = [(Session::class),(LocationPoint::class)],version = 1)
abstract class SessionDB : RoomDatabase(){
    abstract fun sessionDao(): SessionDao
    abstract fun locationPointDao():LocationPointDao

    companion object{
        private var sInstance: SessionDB? = null
        @Synchronized
        fun get(context: Context): SessionDB {
            if (sInstance == null) {
                sInstance =
                    Room.databaseBuilder(context.applicationContext,
                        SessionDB::class.java, "sessions.db").build()
            }
            return sInstance!!
        }
    }
}