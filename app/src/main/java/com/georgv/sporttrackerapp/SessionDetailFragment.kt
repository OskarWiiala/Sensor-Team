package com.georgv.sporttrackerapp

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
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
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.advancedpolyline.PolychromaticPaintList

class SessionDetailFragment : Fragment(R.layout.fragment_session_detail) {
    private lateinit var mapView: MapView
    private lateinit var distanceView: TextView
    private lateinit var averageSpeedView: TextView
    private lateinit var stepsView: TextView
    private lateinit var caloriesView: TextView
    private var activityContext: Context? = null

    private lateinit var session: TrackedSession
    private var _sessionID: Long = 0
    fun getSessionID(id: Long) {
        _sessionID = id
    }

    private lateinit var marker: Marker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityContext = activity?.applicationContext
        mapView = view.findViewById(R.id.mapView2)
        distanceView = view.findViewById(R.id.historyDetailDistance)
        averageSpeedView = view.findViewById(R.id.historyDetailSpeed)
        stepsView = view.findViewById(R.id.historyDetailSteps)
        caloriesView = view.findViewById(R.id.historyDetailCalories)

        marker = Marker(mapView)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        Log.d(_sessionID.toString(), "ID")
        lifecycleScope.launch(Dispatchers.Main) {
            fetchFromDatabase()
        }

    }

    private fun fetchFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val sessionListDao: SessionDao = SessionDB.get(requireContext()).sessionDao()
            session = sessionListDao.getTrackedSessionById(_sessionID)
            val locationGeoPoints = mutableListOf<GeoPoint>()
            for (item in session.locationPoints!!) {
                locationGeoPoints.add(GeoPoint(item.latitude, item.longtitude))
            }

            lifecycleScope.launch(Dispatchers.Main) {
                if (locationGeoPoints.isNotEmpty()) {
                    val lastGeoPoint = locationGeoPoints.last()
                    distanceView.text =
                        (getString(R.string.history_detail_distance) + " " + session.session?.distance?.let {
                            TypeConverterUtil().meterToKilometerConverter(
                                it
                            )
                        } + " km")
                    averageSpeedView.text =
                        (getString(R.string.history_detail_average_speed) + " " + session.session?.averageSpeed?.let {
                            TypeConverterUtil().msToKmhConverter(
                                it
                            )
                        } + " km/h")

                    stepsView.text =
                        (getString(R.string.history_detail_steps) + " " + session.session?.steps.toString())
                    caloriesView.text =
                        (getString(R.string.history_detail_calories) + " " + session.session?.calories.toString())


                    mapView.controller.setCenter(lastGeoPoint)
                    marker.position = lastGeoPoint
                    marker.icon = activityContext?.let {
                        AppCompatResources.getDrawable(
                            it,
                            R.drawable.ic_location_on_24
                        )
                    }
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    val mapController = mapView.controller
                    mapController.setZoom(18.0)
                    mapController.animateTo(marker.position)
                    marker.closeInfoWindow()
                    mapView.overlays.add(marker)
                    mapView.invalidate()


                    for ((counter, item) in session.locationPoints.withIndex()) {
                        // handles drawing a line between GeoPoints in map. Also assigns a color to the line based on speed.
                        val line = Polyline()
                        val pPaint = Paint()
                        pPaint.strokeWidth = 10F

                        // returns the correct color for the line based on speed
                        val pColorMap = PolylineColorUtil(requireContext(), item.currentSpeed)

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
                    Log.d("SDF","locationGeoPoints array is empty")
                }
            }
        }
    }
}


