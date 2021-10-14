package com.georgv.sporttrackerapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.georgv.sporttrackerapp.data.GraphListData
import com.georgv.sporttrackerapp.data.LocationPoint
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.data.TrackedSession
import kotlinx.coroutines.flow.Flow


@Dao
interface SessionDao {
    @Query("SELECT * FROM session WHERE isRunning = :running")
    fun getAllFinishedSessions(running: Boolean): LiveData<List<Session>>

    @Query("SELECT * FROM session WHERE isRunning = :bool")
    fun getRunningSessionAsLiveData(bool:Boolean):LiveData<TrackedSession>

    @Query("SELECT * FROM session WHERE isRunning = :bool")
    fun getRunningSession(bool:Boolean):Session

    @Query("SELECT * FROM session WHERE id = :id")
    fun getSessionFlowById(id:Long): Flow<Session>

    @Query("SELECT * FROM session WHERE id = :id")
    fun getTrackedSessionById(id:Long):TrackedSession

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: Session): Long

    @Query("UPDATE session SET isRunning=:isRunning, endTime=:endTime WHERE id=:id")
    fun finalSessionUpdate(isRunning: Boolean?, endTime: Long?, id: Long)

    @Query("UPDATE session SET isRunning=:isRunning, endTime=:endTime,distance=:distance,averageSpeed=:averageSpeed,steps=:steps,calories=:calories WHERE id = :id")
    fun update(isRunning: Boolean?,endTime:Long?,distance:Float, averageSpeed:Float, steps:Long, calories:Double, id: Long)

    @Query("SELECT endTime, distance, averageSpeed, steps, calories FROM session")
    fun getGraphVariables(): List<GraphListData>

    @Query("DELETE FROM session WHERE id=:id")
    fun deleteById(id:Long)

    @Delete
    fun delete(session: Session)

}


@Dao
interface LocationPointDao{
    @Insert
    fun insert(locationPoint: LocationPoint?): Long

    @Query("SELECT * FROM locationpoint WHERE sessionID =:id")
    fun getBySessionLiveData(id:Long?):LiveData<LocationPoint>

    @Query("SELECT * FROM locationpoint WHERE sessionID = :id")
    fun getBySessionId(id: Long):List<LocationPoint>

    @Query("DELETE FROM locationpoint WHERE sessionID = :id")
    fun deleteLocationsBySession(id:Long)
}