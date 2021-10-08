package com.georgv.sporttrackerapp.graph

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.GraphListData
import com.georgv.sporttrackerapp.database.SessionDB
import com.georgv.sporttrackerapp.database.SessionDao
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.util.*

class GraphDataToday(val context: Application) {

    // This holds data to be inserted into DataPoints
    private val hourList: MutableList<Double> = mutableListOf(
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0
    )

    // all sessions from database
    private lateinit var allSessions: List<GraphListData>

    private var sessionsToday: MutableList<GraphListData> = mutableListOf()
    private val sessionListDao: SessionDao = SessionDB.get(context).sessionDao()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createGraphData(selectedVariable: String): LineGraphSeries<DataPoint> = withContext(
        Dispatchers.IO
    ) {
        val date = Date()
        val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val year = localDate.year
        val month = localDate.monthValue
        val day = localDate.dayOfMonth
        val currentYearMonthDay = year.toString() + month.toString() + day.toString()

        allSessions = sessionListDao.getGraphVariables()

        // Adds all relevant sessions from allSessions to sessionsToday
        for (item in allSessions) {
            val itemDate = TypeConverterUtil().fromTimestamp(item.endTime)
            val itemLocalDate =
                itemDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val itemYear = itemLocalDate.year
            val itemMonth = itemLocalDate.monthValue
            val itemDay = itemLocalDate.dayOfMonth
            val itemYearMonthDay = itemYear.toString() + itemMonth.toString() + itemDay.toString()

            if (itemYearMonthDay == currentYearMonthDay) {
                sessionsToday.add(item)
            }
        }

        val cal = Calendar.getInstance()
        var counter = 1
        var itemHour = 0
        var averageSpeed = 0.0

        // Assigns values to hourList
        for (item in sessionsToday) {
            val itemDate = TypeConverterUtil().fromTimestamp(item.endTime)
            cal.time = itemDate!!
            itemHour = cal[Calendar.HOUR_OF_DAY]
            if (selectedVariable == "Distance") {
                hourList[itemHour] += item.distance.toDouble()
            }
            if (selectedVariable == "Average speed") {
                hourList[itemHour] = hourList[itemHour] + item.averageSpeed.toDouble()
                averageSpeed = hourList[itemHour] / counter
            }
            if (selectedVariable == "Steps") {
                hourList[itemHour] += item.steps.toDouble()
            }
            if (selectedVariable == "Calories") {
                hourList[itemHour] += item.calories.toDouble()
            }
            counter++
        }

        if(selectedVariable == "Average speed") {
            hourList[itemHour] = averageSpeed
        }

        // LineGraphSeries is used in generating graph data using DataPoints
        return@withContext LineGraphSeries(
            arrayOf(
                DataPoint(0.0, hourList[0]),
                DataPoint(1.0, hourList[1]),
                DataPoint(2.0, hourList[2]),
                DataPoint(3.0, hourList[3]),
                DataPoint(4.0, hourList[4]),
                DataPoint(5.0, hourList[5]),
                DataPoint(6.0, hourList[6]),
                DataPoint(7.0, hourList[7]),
                DataPoint(8.0, hourList[8]),
                DataPoint(9.0, hourList[9]),
                DataPoint(10.0, hourList[10]),
                DataPoint(11.0, hourList[11]),
                DataPoint(12.0, hourList[12]),
                DataPoint(13.0, hourList[13]),
                DataPoint(14.0, hourList[14]),
                DataPoint(15.0, hourList[15]),
                DataPoint(16.0, hourList[16]),
                DataPoint(17.0, hourList[17]),
                DataPoint(18.0, hourList[18]),
                DataPoint(19.0, hourList[19]),
                DataPoint(20.0, hourList[20]),
                DataPoint(21.0, hourList[21]),
                DataPoint(22.0, hourList[22]),
                DataPoint(23.0, hourList[23]),
            )
        )
    }
}
