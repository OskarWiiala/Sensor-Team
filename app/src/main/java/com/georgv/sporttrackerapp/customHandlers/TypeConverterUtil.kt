package com.georgv.sporttrackerapp.customHandlers

import androidx.room.TypeConverter
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

    fun msToKmhConverter(speed: Float): Float {
        return String.format(null,"%.2f", speed * 3.6f).toFloat()
    }

    fun meterToKilometerConverter(distance: Float): Double {
        return String.format(null,"%.2f", distance / 1000).toDouble()
    }
}