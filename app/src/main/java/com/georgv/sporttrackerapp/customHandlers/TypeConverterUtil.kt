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

    fun locationPointsToGeoPoints(myList: List<LocationPoint>):List<GeoPoint>{
        val l = mutableListOf<GeoPoint>()
        for(lp in myList){
            val geoPoint = GeoPoint(lp.latitude,lp.longtitude)
            l.add(geoPoint)
        }
        return l
    }

    // does not check if day is changed, this function assumes that the session start time and end time is done on the same day.
    fun durationFromHourMinuteSecond(startHour:Int, startMinute:Int, startSecond:Int, endHour:Int, endMinute:Int, endSecond:Int): String {
        var resultSecond = endSecond - startSecond
        var resultMinute = endMinute - startMinute
        var resultHour = endHour - startHour
        if(resultSecond < 0) {
            resultSecond += 60
            resultMinute -= 1
        }
        if(resultMinute < 0) {
            resultMinute += 60
            resultHour -= 1
        }
        return ("$resultHour:$resultMinute:$resultSecond")
    }
}