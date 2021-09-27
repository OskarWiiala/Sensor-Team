package com.georgv.sporttrackerapp.data

import androidx.room.Entity
import org.osmdroid.util.GeoPoint
import java.util.*

@Entity
data class Session(val startTime:Date,
                   val endTime:Date,
                   val startPos: GeoPoint,
                   val endPos: GeoPoint,
                   val distance:Double,
                   val currentSpeed:Float,
                   val averageSpeed:Float,
                   val steps:Int,
                   val calories:Int) {


}