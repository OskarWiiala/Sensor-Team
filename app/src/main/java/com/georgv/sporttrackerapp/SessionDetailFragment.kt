package com.georgv.sporttrackerapp

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.georgv.sporttrackerapp.customHandlers.PolylineColorUtil
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.TrackedSession
import com.georgv.sporttrackerapp.database.SessionDB
import com.georgv.sporttrackerapp.database.SessionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.advancedpolyline.PolychromaticPaintList
import java.time.ZoneId
import java.util.*

class SessionDetailFragment : Fragment(R.layout.fragment_session_detail) {
    private lateinit var mapView: MapView
    private lateinit var dateView: TextView
    private lateinit var distanceView: TextView
    private lateinit var averageSpeedView: TextView
    private lateinit var stepsView: TextView
    private lateinit var caloriesView: TextView
    private lateinit var durationView: TextView
    private var activityContext: Context? = null

    private lateinit var session: TrackedSession
    private var _sessionID: Long = 0
    fun getSessionID(id: Long) {
        _sessionID = id
    }

    private lateinit var marker: Marker

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityContext = activity?.applicationContext
        mapView = view.findViewById(R.id.mapView2)
        dateView = view.findViewById(R.id.historyDetailDate)
        distanceView = view.findViewById(R.id.historyDetailDistance)
        averageSpeedView = view.findViewById(R.id.historyDetailSpeed)
        stepsView = view.findViewById(R.id.historyDetailSteps)
        caloriesView = view.findViewById(R.id.historyDetailCalories)
        durationView = view.findViewById(R.id.historyDetailDuration)

        marker = Marker(mapView)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.zoomController.setVisibility(
            CustomZoomButtonsController.Visibility.NEVER)

        Log.d(_sessionID.toString(), "ID")
        lifecycleScope.launch(Dispatchers.Main) {
            fetchFromDatabase()
        }
    }

    // Fetches items from correct session by session id from the database and assigns the values to the corresponding UI elements
    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val sessionListDao: SessionDao = SessionDB.get(requireContext()).sessionDao()
            session = sessionListDao.getTrackedSessionById(_sessionID)
            val locationGeoPoints = mutableListOf<GeoPoint>()
            for (item in session.locationPoints) {
                locationGeoPoints.add(GeoPoint(item.latitude, item.longtitude))
            }

            lifecycleScope.launch(Dispatchers.Main) {
                if (locationGeoPoints.isNotEmpty()) {
                    val lastGeoPoint = locationGeoPoints.last()
                    val item = session.session
                    distanceView.text =
                        (getString(R.string.history_detail_distance) + " " + item?.distance?.let {
                            TypeConverterUtil().meterToKilometerConverter(
                                it
                            )
                        } + " km")
                    averageSpeedView.text =
                        (getString(R.string.history_detail_average_speed) + " " + item?.averageSpeed?.let {
                            TypeConverterUtil().msToKmhConverter(
                                it
                            )
                        } + " km/h")

                    stepsView.text =
                        (getString(R.string.history_detail_steps) + " " + item?.steps.toString())
                    caloriesView.text =
                        (getString(R.string.history_detail_calories) + " " + item?.calories.toString())

                    // Uses Day/Month/Year Hour/Minute as displayed text for dateView. also handles duration for the view
                    val itemDateStart = TypeConverterUtil().fromTimestamp(item?.startTime)
                    val itemLocalDate =
                        itemDateStart!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    val itemYear = itemLocalDate.year
                    val itemMonth = itemLocalDate.month.toString().take(3)
                    val itemDay = itemLocalDate.dayOfMonth

                    val cal = Calendar.getInstance()
                    cal.time = itemDateStart
                    val itemHourStart = cal[Calendar.HOUR_OF_DAY]
                    val itemMinuteStart = cal[Calendar.MINUTE]
                    val itemSecondStart = cal[Calendar.SECOND]

                    val itemDateEnd = TypeConverterUtil().fromTimestamp(item?.endTime)
                    val cal2 = Calendar.getInstance()
                    cal2.time = itemDateEnd!!
                    val itemHourEnd = cal2[Calendar.HOUR_OF_DAY]
                    val itemMinuteEnd = cal2[Calendar.MINUTE]
                    val itemSecondEnd = cal2[Calendar.SECOND]

                    val itemDisplayDate = ("$itemDay $itemMonth $itemYear")
                    val itemDisplayHourMinute = TypeConverterUtil().hourMinuteToCorrectFormat(itemHourEnd, itemMinuteEnd)
                    val itemDisplayDuration = TypeConverterUtil().durationFromHourMinuteSecond(
                        itemHourStart,
                        itemMinuteStart,
                        itemSecondStart,
                        itemHourEnd,
                        itemMinuteEnd,
                        itemSecondEnd
                    )
                    dateView.text = ("$itemDisplayDate $itemDisplayHourMinute")
                    durationView.text = (itemDisplayDuration)


                    mapView.controller.setCenter(lastGeoPoint)
                    marker.position = lastGeoPoint
                    marker.icon = activityContext?.let {
                        AppCompatResources.getDrawable(
                            it,
                            R.drawable.ic_location_on_24
                        )
                    }

                    // sets up anchor, controller and marker for the map
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    val mapController = mapView.controller
                    mapController.setZoom(18.0)
                    mapController.animateTo(marker.position)
                    marker.closeInfoWindow()
                    mapView.overlays.add(marker)
                    mapView.invalidate()

                    for ((counter, sessionItem) in session.locationPoints.withIndex()) {
                        // handles drawing a line between GeoPoints in map. Also assigns a color to the line based on speed.
                        val line = Polyline()
                        val pPaint = Paint()
                        pPaint.strokeWidth = 10F

                        // returns the correct color for the line based on speed
                        val pColorMap = PolylineColorUtil(requireContext(), sessionItem.currentSpeed)

                        // handles adding the correct GeoPoints to the line which are used to assign the correct line color based on user's speed.
                        if (counter == 0 || counter == 1) {
                            val myArray = listOf(locationGeoPoints[0])
                            line.setPoints(myArray)
                        } else {
                            val geoPointList = mutableListOf<GeoPoint>()
                            val secondLast2 = locationGeoPoints[counter - 1]
                            val last2 = locationGeoPoints[counter]
                            geoPointList.add(secondLast2)
                            geoPointList.add(last2)
                            line.setPoints(geoPointList)
                        }

                        line.outlinePaintLists.add(PolychromaticPaintList(pPaint, pColorMap, false))
                        mapView.overlays.add(line)
                    }
                } else {
                    Log.d("SDF", "locationGeoPoints array is empty")
                }
            }
        }
    }
}


