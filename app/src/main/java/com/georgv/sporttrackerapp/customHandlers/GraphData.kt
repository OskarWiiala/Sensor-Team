package com.georgv.sporttrackerapp.customHandlers

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import com.georgv.sporttrackerapp.graph.GraphDataThisMonth
import com.georgv.sporttrackerapp.graph.GraphDataThisWeek
import com.georgv.sporttrackerapp.graph.GraphDataThisYear
import com.georgv.sporttrackerapp.graph.GraphDataToday
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

// Run in background due to making a request to the database
class GraphData(application: Application): AndroidViewModel(application) {
    private val appContext = application
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getGraphDataPointsOfToday(selectedVariable: String): LineGraphSeries<DataPoint> {
        return GraphDataToday(appContext).createGraphData(selectedVariable)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getGraphDataPointsOfThisWeek(selectedVariable: String): LineGraphSeries<DataPoint> {
        return GraphDataThisWeek(appContext).createGraphData(selectedVariable)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getGraphDataPointsOfThisMonth(selectedVariable: String): LineGraphSeries<DataPoint> {
        return GraphDataThisMonth(appContext).createGraphData(selectedVariable)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getGraphDataPointsOfThisYear(selectedVariable: String): LineGraphSeries<DataPoint> {
        return GraphDataThisYear(appContext).createGraphData(selectedVariable)
    }
}