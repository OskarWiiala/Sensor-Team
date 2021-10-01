package com.georgv.sporttrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val startTime: Long?,
    val endTime: Long?,
    val geopoints: List<Pair<Long,Long>>,
    val distance: Float,
    val currentSpeed: Float,
    val averageSpeed: Float,
    val steps: Int,
    val calories: Int
) {


}