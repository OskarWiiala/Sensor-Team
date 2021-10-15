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
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.georgv.sporttrackerapp.data.TrackedSession
import com.georgv.sporttrackerapp.viewmodel.SessionViewModel
import com.georgv.sporttrackerapp.viewmodel.TrackedSessionLiveData
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var timerText: TextView

    private var btnStart: MaterialButton? = null
    private var btnStop: ImageButton? = null

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
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        marker = Marker(mapView)

        Permissions().askForPermissions(
            "ACCESS_FINE_LOCATION + ACTIVITY_RECOGNITION",
            requireActivity()
        )

        observeData()

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
        Log.d("observeData", "we got here")
        setOnlyMap()
        val sessionObserver = Observer<TrackedSession> { session ->
            if (session != null) {
                setRunningView()
                travelDistance.text =
                    (getString(R.string.travel_distance) + " " + session.session?.distance?.let {
                        TypeConverterUtil().meterToKilometerConverter(it)
                    } + " km")
                travelSteps.text =
                    (getString(R.string.travel_steps) + " " + session.session?.steps.toString())
                travelCalories.text =
                    (getString(R.string.travel_calories) + " " + session.session?.calories.toString())

                if (session.locationPoints.isNotEmpty()) {
                    travelSpeed.text =
                        (getString(R.string.travel_speed) + " " + TypeConverterUtil().msToKmhConverter(
                            session.locationPoints.last().currentSpeed
                        ) + " km/h")

                    val list = TypeConverterUtil().locationPointsToGeoPoints(session.locationPoints)
                    drawLine(list, session)
                }
            } else {
                setDefaultView()
            }
        }
        svm.session.observe(viewLifecycleOwner, sessionObserver)
    }

    private fun drawLine(list: List<GeoPoint>, session: TrackedSession) {
        for ((counter, _) in list.withIndex()) {
            val line = Polyline()
            val pPaint = Paint()
            pPaint.strokeWidth = 10F
            val currentSpeed = session.locationPoints[counter].currentSpeed
            val pColorMap = PolylineColorUtil(
                requireContext(),
                currentSpeed
            )
            // handles adding the correct GeoPoints to the line which are used to assign the correct line color based on user's speed.
            if (counter == 0 || counter == 1) {
                val myArray = listOf(list[0])
                line.setPoints(myArray)
            } else {
                val geoPointList = mutableListOf<GeoPoint>()
                val secondLast2 = list[counter - 1]
                val last2 = list[counter]
                geoPointList.add(secondLast2)
                geoPointList.add(last2)
                line.setPoints(geoPointList)
            }

            line.outlinePaintLists.add(PolychromaticPaintList(pPaint, pColorMap, false))
            mapView.overlays.add(line)
            setLocationMarker(session.locationPoints.last())
        }
    }

    private fun setViews(view: View) {
        btnStart = view.findViewById(R.id.btnStart)
        btnStop = view.findViewById(R.id.btnStop)
        constraintLayout = view.findViewById(R.id.startSessionLayout)

        travelDistance = view.findViewById(R.id.travelDistance)
        travelSpeed = view.findViewById(R.id.travelSpeed)
        travelSteps = view.findViewById(R.id.travelSteps)
        travelCalories = view.findViewById(R.id.travelCalories)
        timerText = view.findViewById(R.id.timerText)
        mapView = view.findViewById(R.id.mapView)

    }

    private fun setOnlyMap() {
        btnStart?.visibility = View.GONE
        constraintLayout.visibility = View.GONE
    }

    private fun setRunningView() {
        constraintLayout.visibility = View.VISIBLE
        btnStart?.animate()?.translationY(500f)
        constraintLayout.animate().translationY(0f)

    }

    private fun setDefaultView() {
        btnStart?.visibility = View.VISIBLE
        btnStart?.animate()?.translationY(0f)
        constraintLayout.animate().translationY(500f)
    }

    private fun startTrackingSession() {
        val perms = ActivityCompat.checkSelfPermission(
            activityContext!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        when (perms) {
            0 -> {
                try {
                    btnStart?.setEnabled(false)
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
                        btnStart?.setEnabled(true)
                        marker = Marker(mapView)
                        mapView.overlays.clear()
                        // Type check for user weight input
                        val userInputIntOrNull = userInput.text.toString().toIntOrNull()
                        if (userInput.text.isNotEmpty() && userInputIntOrNull != null) {
                            val userWeightKg = userInput.text.toString().toDouble()
                            TrackedSessionLiveData.weight = userWeightKg
                            svm.startSession()
                            TrackedSessionLiveData.startLocationUpdates()
                        } else {
                            Log.d("TSF", "user weight is in incorrect format or is empty")
                            btnStart?.setEnabled(true)
                        }
                    }

                    // When user cancels popup interface
                    builder.setNegativeButton(
                        "Cancel"
                    ) { _, _ ->
                        Log.d("cancel", "canceled dialog interface")
                        btnStart?.setEnabled(true)
                    }

                    // Puts the popup to the screen
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                    dialog.setOnDismissListener{
                        btnStart?.setEnabled(true)
                    }




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
            val activity = requireView().context as MainActivity
            TrackedSessionLiveData.stopLocationUpdates()
            svm.stopSession()
            counter = 0

            val notificationBuilder = NotificationCompat.Builder(
                requireContext(),
                getString(R.string.notification_channel_1)
            )
                .setSmallIcon(R.drawable.ic_save_24)
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
            val manager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}