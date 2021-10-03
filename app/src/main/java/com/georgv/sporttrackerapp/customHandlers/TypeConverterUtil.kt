package com.georgv.sporttrackerapp.customHandlers

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*

class TypeConverterUtil {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

//    @TypeConverter
//    fun fromString(value: Double): List<Double> {
//        val listType: Type = object : TypeToken<List<Double>?>() {}.type
////        return Gson().fromJson(value, listType)
//        return Gson().fromJson(value, listType)
//    }
//
//    @TypeConverter
//    fun fromArrayList(list: ArrayList<String?>?): String? {
//        val gson = Gson()
//        return gson.toJson(list)
//    }
}