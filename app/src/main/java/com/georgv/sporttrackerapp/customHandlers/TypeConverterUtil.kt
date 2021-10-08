package com.georgv.sporttrackerapp.customHandlers

import androidx.room.TypeConverter
import com.georgv.sporttrackerapp.data.LocationPoint
import com.google.gson.Gson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint
import java.lang.reflect.Type


class TypeConverterUtil {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }


    fun msToKmhConverter(speed: Float): Float {

        return String.format(null,"%.2f", speed * 3.6f).toFloat()
    }

    fun meterToKilometerConverter(distance: Double): Double {
        return String.format(null,"%.2f", distance / 1000).toDouble()
    }



}