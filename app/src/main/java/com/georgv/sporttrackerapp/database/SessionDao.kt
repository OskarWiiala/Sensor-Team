package com.georgv.sporttrackerapp.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.georgv.sporttrackerapp.data.GraphListData
import com.georgv.sporttrackerapp.data.LocationPoint
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.data.TrackedSession

@Dao
interface SessionDao {
    @Query("SELECT * FROM session")
    fun getAll(): LiveData<List<Session>>

    @Query("SELECT * FROM session WHERE id = :id")
    fun getSessionById(id:Long): LiveData<Session>

    @Query("SELECT * FROM session WHERE isRunning = 1")
    fun getRunningSession():Session

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insert(session: Session): Long



    @Query("UPDATE session SET isRunning=:isRunning, endTime=:endTime,distance=:distance,averageSpeed=:averageSpeed,steps=:steps,calories=:calories WHERE id = :id")
    fun update(
        isRunning: Boolean?,
        endTime: Long?,
        distance:Float, averageSpeed:Float, steps:Long, calories:Double, id: Long)

    @Query("UPDATE session SET isRunning=:isRunning, endTime=:endTime WHERE id=:id")
    fun endSessionUpdate(
        isRunning: Boolean?,
        endTime: Long?, id: Long)

    @Query("SELECT endTime, distance, averageSpeed, steps, calories FROM session")
    fun getGraphVariables(): List<GraphListData>

}


@Dao
interface LocationPointDao{
    @Insert
    fun insert(locationPoint: LocationPoint?): Long

    @Query("SELECT * FROM locationpoint WHERE sessionID = :id")
    fun getBySessionId(id: Long):List<LocationPoint>
}