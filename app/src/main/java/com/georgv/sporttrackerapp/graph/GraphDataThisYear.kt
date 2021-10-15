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

class GraphDataThisYear(context: Application) {

    // This holds data to be inserted into DataPoints
    private val monthList: MutableList<Double> = mutableListOf(
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
    )

    // all sessions from database
    private lateinit var allSessions: List<GraphListData>

    private var sessionsThisYear: MutableList<GraphListData> = mutableListOf()
    private val sessionListDao: SessionDao = SessionDB.get(context).sessionDao()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createGraphData(selectedVariable: String): LineGraphSeries<DataPoint> = withContext(
        Dispatchers.IO
    ) {
        val date = Date()
        val calendarCurrent = Calendar.getInstance()
        calendarCurrent.time = date
        val currentYear = calendarCurrent[Calendar.YEAR]

        allSessions = sessionListDao.getGraphVariables()

        // Adds all relevant sessions from allSessions to sessionsThisYear based on date
        for(item in allSessions) {
            val itemDate = TypeConverterUtil().fromTimestamp(item.endTime)
            val calendarItem = Calendar.getInstance()
            calendarItem.time = itemDate!!
            val itemYear = calendarItem[Calendar.YEAR]

            if (itemYear == currentYear) {
                sessionsThisYear.add(item)
            }
        }

        val cal = Calendar.getInstance()
        var counter = 1
        var itemMonth = 0
        var averageSpeed = 0.0

        // Assigns values to monthList
        for(item in sessionsThisYear) {
            val itemDate = TypeConverterUtil().fromTimestamp(item.endTime)
            val localItemDate = itemDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            cal.time = itemDate
            itemMonth = localItemDate.monthValue -1
            if (selectedVariable == "Distance") {
                monthList[itemMonth] += TypeConverterUtil().meterToKilometerConverter(item.distance)
            }
            if (selectedVariable == "Average speed") {
                monthList[itemMonth] += item.averageSpeed.toDouble()
                averageSpeed = monthList[itemMonth] / counter
            }
            if (selectedVariable == "Steps") {
                monthList[itemMonth] += item.steps.toDouble()
            }
            if (selectedVariable == "Calories") {
                monthList[itemMonth] += item.calories.toDouble()
            }
            counter++
        }

        if (selectedVariable == "Average speed") {
            monthList[itemMonth] = averageSpeed
        }

        // LineGraphSeries is used in generating graph data using DataPoints
        return@withContext LineGraphSeries(
            arrayOf(
                DataPoint(1.0, monthList[0]),
                DataPoint(2.0, monthList[1]),
                DataPoint(3.0, monthList[2]),
                DataPoint(4.0, monthList[3]),
                DataPoint(5.0, monthList[4]),
                DataPoint(6.0, monthList[5]),
                DataPoint(7.0, monthList[6]),
                DataPoint(8.0, monthList[7]),
                DataPoint(9.0, monthList[8]),
                DataPoint(10.0, monthList[9]),
                DataPoint(11.0, monthList[10]),
                DataPoint(12.0, monthList[11]),
            )
        )
    }
}