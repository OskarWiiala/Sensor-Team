package com.georgv.sporttrackerapp.graph

import android.app.Application
import android.os.Build
import android.util.Log
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

class GraphDataThisMonth(val context: Application) {
    private val dayList: MutableList<Double> = mutableListOf(
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
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0
    )

    private lateinit var allSessions: List<GraphListData>
    private var sessionsThisMonth: MutableList<GraphListData> = mutableListOf()
    private val sessionListDao: SessionDao = SessionDB.get(context).sessionDao()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createGraphData(selectedVariable: String): LineGraphSeries<DataPoint> = withContext(
        Dispatchers.IO
    ) {
        val date = Date()
        val calendarCurrent = Calendar.getInstance()
        Log.d("calendar", "calendarCurrent: $calendarCurrent")
        calendarCurrent.time = date
        val currentYear = calendarCurrent[Calendar.YEAR]
        val currentMonth = calendarCurrent[Calendar.MONTH] + 1
        val currentYearMonth = currentYear.toString() + currentMonth.toString()
        Log.d("Date2Month", "currentYearMonth: $currentYearMonth")

        allSessions = sessionListDao.getGraphVariables()

        for (item in allSessions) {
            val itemDate = TypeConverterUtil().fromTimestamp(item.endTime)
            val calendarItem = Calendar.getInstance()
            Log.d("calendar", "calendarItem: $calendarItem")
            calendarItem.time = itemDate!!
            val itemYear = calendarItem[Calendar.YEAR]
            val itemMonth = calendarItem[Calendar.MONTH] + 1
            val itemYearMonth = itemYear.toString() + itemMonth.toString()

            Log.d("itemYearMonth", itemYearMonth)

            if (itemYearMonth == currentYearMonth) {
                sessionsThisMonth.add(item)
            }
        }

        val cal = Calendar.getInstance()
        var counter = 1
        var itemDay = 0
        var averageSpeed = 0.0
        for (item in sessionsThisMonth) {
            Log.d("month","got to item in sessionsThisMonth")
            val itemDate = TypeConverterUtil().fromTimestamp(item.endTime)
            val localItemDate = itemDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            cal.time = itemDate
            itemDay = localItemDate.dayOfMonth -1
            Log.d("month","day of month: $itemDay")
            if (selectedVariable == "Distance") {
                dayList[itemDay] += item.distance.toDouble()
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

        if (selectedVariable == "Average speed") {
            dayList[itemDay] = averageSpeed
        }

        return@withContext LineGraphSeries(
            arrayOf(
                DataPoint(1.0, dayList[0]),
                DataPoint(2.0, dayList[1]),
                DataPoint(3.0, dayList[2]),
                DataPoint(4.0, dayList[3]),
                DataPoint(5.0, dayList[4]),
                DataPoint(6.0, dayList[5]),
                DataPoint(7.0, dayList[6]),
                DataPoint(8.0, dayList[7]),
                DataPoint(9.0, dayList[8]),
                DataPoint(10.0, dayList[9]),
                DataPoint(11.0, dayList[10]),
                DataPoint(12.0, dayList[11]),
                DataPoint(13.0, dayList[12]),
                DataPoint(14.0, dayList[13]),
                DataPoint(15.0, dayList[14]),
                DataPoint(16.0, dayList[15]),
                DataPoint(17.0, dayList[16]),
                DataPoint(18.0, dayList[17]),
                DataPoint(19.0, dayList[18]),
                DataPoint(20.0, dayList[19]),
                DataPoint(21.0, dayList[20]),
                DataPoint(22.0, dayList[21]),
                DataPoint(23.0, dayList[22]),
                DataPoint(24.0, dayList[23]),
                DataPoint(25.0, dayList[24]),
                DataPoint(26.0, dayList[25]),
                DataPoint(27.0, dayList[26]),
                DataPoint(28.0, dayList[27]),
                DataPoint(29.0, dayList[28]),
                DataPoint(30.0, dayList[29]),
                DataPoint(31.0, dayList[30]),
            )
        )
    }
}