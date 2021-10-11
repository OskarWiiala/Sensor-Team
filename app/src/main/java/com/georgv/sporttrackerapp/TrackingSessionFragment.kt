package com.georgv.sporttrackerapp

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.georgv.sporttrackerapp.customHandlers.CalorieCounter
import com.georgv.sporttrackerapp.customHandlers.Permissions
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.LocationPoint
import com.georgv.sporttrackerapp.viewmodel.SessionViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class TrackingSessionFragment : Fragment() {
    private val svm: SessionViewModel by viewModels()
    private var activityContext: Context? = null

    private lateinit var mapView: MapView
    private lateinit var marker: Marker
    private lateinit var textAddress: TextView
    private lateinit var travelDistance: TextView
    private lateinit var travelSpeed: TextView
    private lateinit var travelSteps: TextView
    private lateinit var travelCalories: TextView

    private lateinit var progressAddress: ProgressBar
    private lateinit var progressDistance: ProgressBar
    private lateinit var progressSpeed: ProgressBar
    private lateinit var progressSteps: ProgressBar
    private lateinit var progressCalories: ProgressBar

    private var btnStart: MaterialButton? = null
    private var btnStop: MaterialButton? = null

    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityContext = activity?.applicationContext
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracking_session, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Configuration.getInstance().load(
            activityContext,
            PreferenceManager.getDefaultSharedPreferences(activityContext)
        )
        super.onViewCreated(view, savedInstanceState)

        initiateValues(view)

        Permissions.askForPermissions(
            "ACCESS_FINE_LOCATION + ACTIVITY_RECOGNITION",
            requireActivity()
        )

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        btnStart?.setOnClickListener {
            startTrackingSession()
        }
        btnStop?.setOnClickListener {
            endTrackingSession()
        }
    }

    private fun setLocationMarker(locationPoint: LocationPoint) {
        val geoPoint = GeoPoint(locationPoint.latitude, locationPoint.longtitude)
        mapView.controller.setCenter(geoPoint)
        marker.position = geoPoint
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
    }


    private fun observeData(weight: Double) {
        val locationObserver = Observer<LocationPoint> { newLocation ->
            travelSpeed.text =
                "Current Speed: " + TypeConverterUtil().msToKmhConverter(newLocation.currentSpeed)
                    .toString() + " KM/H"
            setLocationMarker(newLocation)
        }
        svm.getData().observe(viewLifecycleOwner, locationObserver)

        val distanceObserver = Observer<Float> { totalDistance ->
            travelDistance.text = "Distance: " + totalDistance + " m"
            travelCalories.text =
                "Callories: " + CalorieCounter.countCalories(totalDistance, weight).toString()
        }
        svm.getDistance().observe(viewLifecycleOwner, distanceObserver)

        val stepObserver = Observer<Long> { steps ->
            travelSteps.text = "Steps: " + steps.toString()
        }
        svm.steps().observe(viewLifecycleOwner, stepObserver)

        var currentLoc: GeoPoint
        var previousLoc: GeoPoint

        // handles drawing a line between GeoPoints in map
        val locationArrayObserver = Observer<LocationPoint> { locationArray ->
            Log.d("locationArrayObserver","before currentLoc + counter: $counter")
            currentLoc = GeoPoint(locationArray.latitude, locationArray.longtitude)
            if(counter == 0) {
                Log.d("locationArrayObserver","if previousLoc")
                previousLoc = currentLoc
                svm.addToLocationArray(previousLoc)
            } else {
                Log.d("locationArrayObserver","counter: $counter")
                Log.d("locationArrayObserver","else previousLoc")
                previousLoc = svm.getLocationArray().last()
                svm.addToLocationArray(currentLoc)
            }
            Log.d("locationArrayObserver","before counter")
            counter++
            val line = Polyline()
            line.setPoints(svm.getLocationArray())
            mapView.overlays.add(line)
        }
        svm.getData().observe(viewLifecycleOwner, locationArrayObserver)
    }

    // Initiates some UI elements and handles visibility for progress bars.
    private fun initiateValues(view: View) {
        btnStart = view.findViewById(R.id.btnStart)
        btnStop = view.findViewById(R.id.btnStop)
        textAddress = view.findViewById(R.id.textAddress)
        travelDistance = view.findViewById(R.id.travelDistance)
        travelSpeed = view.findViewById(R.id.travelSpeed)
        travelSteps = view.findViewById(R.id.travelSteps)
        travelCalories = view.findViewById(R.id.travelCalories)
        mapView = view.findViewById(R.id.mapView)

        progressAddress = view.findViewById(R.id.progressAddress)
        progressDistance = view.findViewById(R.id.progressDistance)
        progressSpeed = view.findViewById(R.id.progressSpeed)
        progressSteps = view.findViewById(R.id.progressSteps)
        progressCalories = view.findViewById(R.id.progressCalories)

        progressAddress.visibility = View.GONE
        progressDistance.visibility = View.GONE
        progressSpeed.visibility = View.GONE
        progressSteps.visibility = View.GONE
        progressCalories.visibility = View.GONE

        btnStop?.visibility = View.GONE
    }


    // Starts sports tracking session when user presses start tracking-button
    private fun startTrackingSession() {

        val perms = ActivityCompat.checkSelfPermission(
            activityContext!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        when (perms) {
            0 -> {
                try {
                    Log.d("Location perms", "access 0")
                    // This dialog popup asks the user for their weight in kilograms. It is used in calorie counting
                    val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                    // sets a custom dialog interface for the popup
                    val li = LayoutInflater.from(activityContext)
                    val promptsView = li.inflate(R.layout.weight_prompt, null)
                    builder.setView(promptsView)
                    // InputField to set user's weight
                    val userInput = promptsView.findViewById<EditText>(R.id.editTextDialogUserInput)
                    builder.setCancelable(true)

                    // When user confirms popup interface
                    builder.setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        svm.startSession()

                        Log.d("confirm", "confirmed")
                        progressAddress.visibility = View.VISIBLE
                        progressDistance.visibility = View.VISIBLE
                        progressSpeed.visibility = View.VISIBLE
                        progressSteps.visibility = View.VISIBLE
                        progressCalories.visibility = View.VISIBLE
                        // The numerical value of the user's weight
                        val userWeightKg = userInput.text.toString().toDouble()


                        btnStart?.visibility = View.GONE
                        btnStop?.visibility = View.VISIBLE
                        progressAddress.visibility = View.GONE
                        progressDistance.visibility = View.GONE
                        progressSpeed.visibility = View.GONE
                        progressSteps.visibility = View.GONE
                        progressCalories.visibility = View.GONE
                        marker = Marker(mapView)
                        // Setting up mark
                        observeData(userWeightKg)
                        mapView.overlays.clear()

                    }

                    // When user cancels popup interface
                    builder.setNegativeButton(
                        "Cancel"
                    ) { _, _ ->
                        Log.d("cancel", "canceled dialog interface")
                    }

                    // Puts the popup to the screen
                    val dialog: AlertDialog = builder.create()
                    dialog.show()

                } catch (e: Error) {
                    Log.d("btnStart", "requestLocationUpdates error: $e")
                }
            }
            // if permission for location tracking is denied by user
            -1 -> Log.d("Location perms", "access -1")
            else -> Log.d("Location perms", "neither 0 or -1")
        }
    }


    private fun endTrackingSession() {
        // Creates a dialog popup interface to confirm if user wants to end sports tracking session
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setCancelable(true)
        builder.setTitle("End session")
        builder.setMessage("Are you sure you want to end this session?")

        // When user confirms popup interface
        builder.setPositiveButton(
            "End session"
        ) { _, _ ->
            Log.d("confirm", "confirmed")
            btnStop?.visibility = View.GONE
            btnStart?.visibility = View.VISIBLE
            Log.d("locationArrayObserver","stopped session")
            svm.stopSession()
            counter = 0

        }
        // When user cancels popup interface
        builder.setNegativeButton(
            "keep tracking"
        ) { _, _ -> Log.d("cancel", "canceled dialog interface") }
        // Puts the popup to the screen
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}