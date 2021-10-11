package com.georgv.sporttrackerapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.georgv.sporttrackerapp.data.TrackedSession
import com.georgv.sporttrackerapp.database.SessionDB
import com.georgv.sporttrackerapp.database.SessionDao
import com.georgv.sporttrackerapp.viewmodel.SessionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class SessionDetailFragment : Fragment(R.layout.fragment_session_detail){
    private lateinit var mapView: MapView
    private lateinit var distanceView: TextView
    private lateinit var averageSpeedView: TextView
    private lateinit var stepsView: TextView
    private lateinit var caloriesView: TextView
    private var activityContext: Context? = null

    private lateinit var session: TrackedSession
    private lateinit var marker: Marker

    val svm:SessionViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val session = svm.getSessionById(0)
        //Log.d(session?.averageSpeed.toString(),"TEST")
//    val testTextView:TextView = view.findViewById(R.id.textView)
//        testTextView.text = ("callories:${session?.calories}" +
//                "\ndistance: ${session?.distance}" +
//                "\nsteps: ${session?.steps}" +
//                "\nspeed: ${session?.averageSpeed}")
//

        activityContext = activity?.applicationContext
        mapView = view.findViewById(R.id.mapView2)
        distanceView = view.findViewById(R.id.historyDetailDistance)
        averageSpeedView = view.findViewById(R.id.historyDetailSpeed)
        stepsView = view.findViewById(R.id.historyDetailSteps)
        caloriesView = view.findViewById(R.id.historyDetailCalories)

        marker = Marker(mapView)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        lifecycleScope.launch(Dispatchers.Main) {
            fetchFromDatabase()
        }

   }

    private fun fetchFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val sessionListDao: SessionDao = SessionDB.get(requireContext()).sessionDao()
            session = sessionListDao.getTrackedSessionById(3)
            Log.d("fetchFromDatabase()","session: ${session.session}")
            Log.d("fetchFromDatabase()","locationPoints: ${session.locationPoints}")
            val locationGeoPoints = mutableListOf<GeoPoint>()
            for (item in session.locationPoints!!) {
                locationGeoPoints.add(GeoPoint(item.latitude, item.longtitude))
            }
//            session.value

            lifecycleScope.launch(Dispatchers.Main) {
                distanceView.text = "Distance: " + session.session?.distance.toString()
                averageSpeedView.text = "Average speed: " + session.session?.averageSpeed.toString()
                stepsView.text = "Steps: " + session.session?.steps.toString()
                caloriesView.text = "Calories burnt: " + session.session?.calories.toString()

                val startingGeoPoint = locationGeoPoints[0]
                mapView.controller.setCenter(startingGeoPoint)
                marker.position = startingGeoPoint
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

                val line = Polyline()
                line.setPoints(locationGeoPoints)
                mapView.overlays.add(line)
            }
        }
    }
}


