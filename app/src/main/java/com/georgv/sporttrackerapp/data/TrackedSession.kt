package com.georgv.sporttrackerapp.data

import androidx.room.Embedded
import androidx.room.Relation

class TrackedSession {
    @Embedded
    var session:Session? = null
    @Relation(parentColumn = "id",entityColumn = "sessionID")
    var locationPoints: List<LocationPoint> = listOf<LocationPoint>()
}