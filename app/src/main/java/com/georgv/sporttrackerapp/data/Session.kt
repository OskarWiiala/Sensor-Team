package com.georgv.sporttrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.osmdroid.util.GeoPoint
import java.util.*

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val startTime: Long?,
    val endTime: Long?,
    //val startPos: GeoPoint?,
    //val endPos: GeoPoint?,
    val distance: Double,
    val currentSpeed: Float,
    val averageSpeed: Float,
    val steps: Int,
    val calories: Int
) {


}