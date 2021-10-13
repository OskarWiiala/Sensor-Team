package com.georgv.sporttrackerapp

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Paint
import android.os.Build
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.georgv.sporttrackerapp.customHandlers.Permissions
import com.georgv.sporttrackerapp.customHandlers.PolylineColorUtil
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.LocationPoint
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.viewmodel.SessionViewModel
import com.google.android.material.button.MaterialButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.advancedpolyline.PolychromaticPaintList

class TrackingSessionFragment : Fragment() {
    private val svm: SessionViewModel by viewModels()
    private var activityContext: Context? = null

    private lateinit var mapView: MapView
    private lateinit var marker: Marker
    private lateinit var travelDistance: TextView
    private lateinit var travelSpeed: TextView
    private lateinit var travelSteps: TextView
    private lateinit var travelCalories: TextView

    private lateinit var progressDistance: ProgressBar
    private lateinit var progressSpeed: ProgressBar
    private lateinit var progressSteps: ProgressBar
    private lateinit var progressCalories: ProgressBar

    private var btnStart: MaterialButton? = null
    private var btnStop: MaterialButton? = null

    private lateinit var currentLoc: GeoPoint

    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityContext = activity?.applicationContext
        createNotificationChannel()
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

        setViews(view)

        Permissions().askForPermissions("ACCESS_FINE_LOCATION + ACTIVITY_RECOGNITION", requireActivity())
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


    private fun observeData() {
        val locationObserver = Observer<LocationPoint> { newLocation ->
            travelSpeed.text =
                "Current Speed: " + TypeConverterUtil().msToKmhConverter(newLocation.currentSpeed)
                    .toString() + " KM/H"
            setLocationMarker(newLocation)
        }
        svm.getData().observe(viewLifecycleOwner, locationObserver)

        val sessionObserver = Observer<Session> { session ->
            travelDistance.text = "Distance: " + session.distance.toString()
            travelSteps.text = "Steps: " + session.steps.toString()
            travelCalories.text = "Calories: " + session.calories.toString()
        }
        svm.session?.observe(viewLifecycleOwner, sessionObserver)

        var previousLoc: GeoPoint
        // handles drawing a line between GeoPoints in map
        val locationArrayObserver = Observer<LocationPoint> { locationPoint ->
            Log.d("locationArrayObserver", "before currentLoc + counter: $counter")
            currentLoc = GeoPoint(locationPoint.latitude, locationPoint.longtitude)
            if (counter == 0) {
                mapView.overlays.clear()
                Log.d("locationArrayObserver", "if previousLoc")
                previousLoc = currentLoc
                svm.addToLocationArray(previousLoc)
            } else {
                Log.d("locationArrayObserver", "counter: $counter")
                Log.d("locationArrayObserver", "else previousLoc")
                previousLoc = svm.getLocationArray().last()
                svm.addToLocationArray(currentLoc)
            }
            Log.d("locationArrayObserver", "before counter")

            // handles drawing a line between GeoPoints in map. Also assigns a color to the line based on speed.
            val line = Polyline()
            val pPaint = Paint()
            pPaint.strokeWidth = 10F

            val pColorMap = PolylineColorUtil(requireContext(), locationPoint.currentSpeed)

            // handles adding the correct GeoPoints to the line which are used to assign the correct line color based on user's speed.
            if (counter == 0 || counter == 1) {
                line.setPoints(svm.getLocationArray())
            } else {
                val geoPointList = mutableListOf<GeoPoint>()
                val arraySize = svm.getLocationArray().size
                val secondLast = svm.getLocationArray()[arraySize-2]
                val last = svm.getLocationArray().last()
                geoPointList.add(secondLast)
                geoPointList.add(last)
                line.setPoints(geoPointList)
            }

            line.outlinePaintLists.add(PolychromaticPaintList(pPaint, pColorMap, false))
            mapView.overlays.add(line)
            counter++

        }
        svm.getData().observe(viewLifecycleOwner, locationArrayObserver)
    }

    private fun setViews(view: View) {
        btnStart = view.findViewById(R.id.btnStart)
        btnStop = view.findViewById(R.id.btnStop)

        travelDistance = view.findViewById(R.id.travelDistance)
        travelSpeed = view.findViewById(R.id.travelSpeed)
        travelSteps = view.findViewById(R.id.travelSteps)
        travelCalories = view.findViewById(R.id.travelCalories)
        mapView = view.findViewById(R.id.mapView)

        progressDistance = view.findViewById(R.id.progressDistance)
        progressSpeed = view.findViewById(R.id.progressSpeed)
        progressSteps = view.findViewById(R.id.progressSteps)
        progressCalories = view.findViewById(R.id.progressCalories)


        btnStop?.visibility = View.GONE
    }


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
                        Log.d("confirm", "confirmed")
                        marker = Marker(mapView)
                        svm.startSession()
                        val userWeightKg = userInput.text.toString().toDouble()
                        svm.getData().getWeight(userWeightKg)
                        observeData()
                        setUIOnSessionStart()

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

    private fun setUIOnSessionStart(){
        progressDistance.visibility = View.VISIBLE
        progressSpeed.visibility = View.VISIBLE
        progressSteps.visibility = View.VISIBLE
        progressCalories.visibility = View.VISIBLE

        btnStart?.visibility = View.GONE
        btnStop?.visibility = View.VISIBLE
        progressDistance.visibility = View.GONE
        progressSpeed.visibility = View.GONE
        progressSteps.visibility = View.GONE
        progressCalories.visibility = View.GONE
        mapView.overlays.clear()
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
            svm.stopSession()
            counter = 0

            var notificationBuilder = NotificationCompat.Builder(
                requireContext(),
                getString(R.string.notification_channel_1)
            )
                .setSmallIcon(R.drawable.bonuspack_bubble)
                .setContentTitle("Session Ended")
                .setContentText("Your Session Is Ended And Saved To The Database!")
                .setPriority(NotificationCompat.PRIORITY_MAX)

            with(NotificationManagerCompat.from(requireContext())) {
                notify(R.string.notification_channel_1, notificationBuilder.build())
            }


        }
        // When user cancels popup interface
        builder.setNegativeButton(
            "keep tracking"
        ) { _, _ -> Log.d("cancel", "canceled dialog interface") }
        // Puts the popup to the screen
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_1)
            val descriptionText = getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                getString(R.string.notification_channel_1),
                name,
                importance
            ).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val manager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    interface UserWeightReciever{
        fun getWeight(weight:Double)
    }
}