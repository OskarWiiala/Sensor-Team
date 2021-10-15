package com.georgv.sporttrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val startTime: Long?,
    var isRunning: Boolean,
    var endTime: Long?,
    var distance: Float?,
    var averageSpeed: Float?,
    var steps: Long?,
    var calories: Int?
) {


}