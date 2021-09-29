package com.georgv.sporttrackerapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.georgv.sporttrackerapp.data.Session

@Dao
interface SessionDao {
    @Query("SELECT * FROM session")
    fun getAll(): LiveData<List<Session>>

    @Insert
    fun insert(session: Session): Long
}