package com.georgv.sporttrackerapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Session::class,
        parentColumns = ["id"],
        childColumns = ["sessionID"]
    )]
)
data class LocationPoint(
    @PrimaryKey(autoGenerate = true)
    val locId: Long,
    val sessionID: Long,
    val latitude: Double,
    val longtitude: Double,
    val currentSpeed: Float

) {

}