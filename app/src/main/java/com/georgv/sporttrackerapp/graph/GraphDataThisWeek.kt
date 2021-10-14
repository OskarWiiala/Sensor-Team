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

class GraphDataThisWeek(context: Application) {

    // This holds data to be inserted into DataPoints
    private val dayList: MutableList<Double> = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

    // all sessions from database
    private lateinit var allSessions: List<GraphListData>

    private var sessionsThisWeek: MutableList<GraphListData> = mutableListOf()
    private val sessionListDao: SessionDao = SessionDB.get(context).sessionDao()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createGraphData(selectedVariable: String): LineGraphSeries<DataPoint> = withContext(
        Dispatchers.IO
    ) {
        val date = Date()
        val calendarCurrent = Calendar.getInstance()
        calendarCurrent.time = date
        val currentYear = calendarCurrent[Calendar.YEAR]
        val currentMonth = calendarCurrent[Calendar.MONTH] +1
        val currentWeek = calendarCurrent[Calendar.WEEK_OF_MONTH]
        val currentYearMonthWeek = currentYear.toString() + currentMonth.toString() + currentWeek.toString()

        allSessions = sessionListDao.getGraphVariables()

        // Adds all relevant sessions from allSessions to sessionsThisWeek based on date
        for (item in allSessions) {
            val itemDate = TypeConverterUtil().fromTimestamp(item.endTime)
            val calendarItem = Calendar.getInstance()
            calendarItem.time = itemDate!!
            val itemYear = calendarItem[Calendar.YEAR]
            val itemMonth = calendarItem[Calendar.MONTH] +1
            val itemWeek = calendarItem[Calendar.WEEK_OF_MONTH]
            val itemYearMonthWeek = itemYear.toString() + itemMonth.toString() + itemWeek.toString()

            if (itemYearMonthWeek == currentYearMonthWeek) {
                sessionsThisWeek.add(item)
            }
        }

        val cal = Calendar.getInstance()
        var counter = 1
        var itemDay = 0
        var averageSpeed = 0.0

        // Assigns values to dayList
        for (item in sessionsThisWeek) {
            val itemDate = TypeConverterUtil().fromTimestamp(item.endTime)
            val localItemDate = itemDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            cal.time = itemDate
            itemDay = localItemDate.dayOfWeek.value -1

            if (selectedVariable == "Distance") {
                dayList[itemDay] += TypeConverterUtil().meterToKilometerConverter(item.distance)
            }
            if (selectedVariable == "Average speed") {
                dayList[itemDay] = dayList[itemDay] + item.averageSpeed.toDouble()
                averageSpeed = dayList[itemDay] / counter
            }
            if (selectedVariable == "Steps") {
                dayList[itemDay] += item.steps.toDouble()
            }
            if (selectedVariable == "Calories") {
                dayList[itemDay] += item.calories.toDouble()
            }
            counter++
        }

        if(selectedVariable == "Average speed") {
            dayList[itemDay] = averageSpeed
        }

        // LineGraphSeries is used in generating graph data using DataPoints
        return@withContext LineGraphSeries(
            arrayOf(
                DataPoint(1.0, dayList[0]),
                DataPoint(2.0, dayList[1]),
                DataPoint(3.0, dayList[2]),
                DataPoint(4.0, dayList[3]),
                DataPoint(5.0, dayList[4]),
                DataPoint(6.0, dayList[5]),
                DataPoint(7.0, dayList[6])
            )
        )
    }
}