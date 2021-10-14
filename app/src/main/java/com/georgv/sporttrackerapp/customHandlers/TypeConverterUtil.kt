package com.georgv.sporttrackerapp.customHandlers

import androidx.room.TypeConverter
import com.georgv.sporttrackerapp.data.LocationPoint
import org.osmdroid.util.GeoPoint
import java.util.*


class TypeConverterUtil {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }

    fun msToKmhConverter(speed: Float?): Float {
        if (speed != null) {
            return String.format(null,"%.2f", speed * 3.6f).toFloat()
        }
        return 0.0f
    }

    fun meterToKilometerConverter(distance: Float): Double {
        return String.format(null,"%.2f", distance / 1000).toDouble()
    }

    fun locationPointsToGeoPoints(mylist:List<LocationPoint>):List<GeoPoint>{
        val l = mutableListOf<GeoPoint>()
        for(lp in mylist){
            val geoPoint = GeoPoint(lp.latitude,lp.longtitude)
            l.add(geoPoint)
        }
        return l
    }
}