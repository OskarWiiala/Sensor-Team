package com.georgv.sporttrackerapp

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import com.georgv.sporttrackerapp.customHandlers.CalorieCounter
import com.georgv.sporttrackerapp.customHandlers.Permissions
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.viewmodel.SessionViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException

class TrackingSessionFragment : Fragment(), LocationListener, SensorEventListener {

    companion object {
        private val parentJob = Job()
        private val coroutineScope = CoroutineScope(Dispatchers.Main + parentJob)
        private val secondScope = CoroutineScope(Dispatchers.IO + parentJob)
    }

    private var activityContext: Context? = null
    private var sensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityContext = activity?.applicationContext
    }

    private var currentLatitude: Double = 0.00
    private var currentLongitude: Double = 0.00
    private var previousLoc: GeoPoint = GeoPoint(0.0, 0.0)
    private var addressValue: String = "No address"
    private var running = false
    private var stepCount = 0
    private var currentSpeed = 0.0f
    private var caloriesValue: Double = 0.0
    private var counter: Int = 0
    private var userWeightKg: Double = 0.0
    private var totalDistanceTraveled: Double = 0.0
    private var locationArray: MutableList<GeoPoint> = mutableListOf()
    private var speedArray: MutableList<Float> = mutableListOf()

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

    // Is called every time location changes. Frequency is dictated in startTrackingSession()
    override fun onLocationChanged(p0: Location) {
        // gets the latitude and longitude of current location
        currentLatitude = p0.latitude
        currentLongitude = p0.longitude
        Log.d("accuracy","${p0.accuracy}")
        addressValue = getAddress(p0.latitude, p0.longitude)

        // osmdroid map API uses GeoPoints to calculate location
        val currentLoc = GeoPoint(currentLatitude, currentLongitude)

        if (counter == 0) {
            // is used to make the 2 first GeoPoints
            previousLoc = GeoPoint(currentLatitude, currentLongitude)
            locationArray.add(previousLoc)
        } else {
            previousLoc = locationArray.last()
            locationArray.add(currentLoc)
        }
        whenLocationChanged(p0)
        // Sets the traveled distance. I put this here instead of whenLocationChanged for easier use.
        getDistance(previousLoc, currentLoc, totalDistanceTraveled)
        counter++
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tracking_session, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Configuration.getInstance().load(
            activityContext,
            PreferenceManager.getDefaultSharedPreferences(activityContext)
        )
        super.onViewCreated(view, savedInstanceState)

        initiateValues(view)

        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as
                LocationManager

        mapView.setTileSource(TileSourceFactory.MAPNIK)

        // Adds ability to zoom with 2 fingers (multitouch)
        mapView.setMultiTouchControls(true)

        // Move the center of the map on a default view point with the
        // map controller (e.g. in location change listener)
        mapView.controller.setCenter(GeoPoint(60.17, 24.95))

        marker = Marker(mapView)
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

        // Permission handler. Found in customHandlers Permissions
        Permissions.askForPermissions(
            "ACCESS_FINE_LOCATION + ACTIVITY_RECOGNITION",
            requireActivity()
        )

        btnStart?.setOnClickListener {
            startTrackingSession(lm)
        }
        btnStop?.setOnClickListener {
            endTrackingSession(lm)
        }
    }

    // Initiates some UI elements and handles visibility for progress bars.
    // Is made into a separate function to clean up onViewCreated
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

    private fun whenLocationChanged(loc: Location) {
        try {
            progressAddress.visibility = View.GONE
            progressDistance.visibility = View.GONE
            progressSpeed.visibility = View.GONE
            progressSteps.visibility = View.GONE
            progressCalories.visibility = View.GONE
            // creating GeoPoint from latitude and longitude
            val gp = GeoPoint(loc.latitude, loc.longitude)
            // Setting up mark
            marker.position = gp
            marker.title = getAddress(loc.latitude, loc.longitude)
            marker.closeInfoWindow()
            mapView.overlays.add(marker)
            mapView.invalidate()

            // Creates line between GeoPoints on map (user can see black line between visited locations)
            val line = Polyline()
            line.setPoints(locationArray)
            mapView.overlays.add(line)

            // UI elements must be run on UI thread or else app crashes
            activity?.runOnUiThread() {
                // Centering map to middle of location
                mapView.controller.setCenter(gp)
                mapView.controller.animateTo(gp)
            }

            textAddress.text = ("${getString(R.string.travel_address)} $addressValue")

            currentSpeed = msToKmhConverter(loc.speed)
            travelSpeed.text = ("${getString(R.string.travel_speed)} $currentSpeed km/h")

            // Only adding speed value if not 0 to reduce average speed being too low
            if(currentSpeed != 0.0f) {
            speedArray.add(currentSpeed)
            }

            caloriesValue = countCalories(
                totalDistanceTraveled,
                userWeightKg
            )
            travelCalories.text = ("${getString(R.string.travel_calories)} $caloriesValue")
        } catch (e: Error) {
            Log.d("whenLocationChanged()", "whenLocationChanged() error: $e")
        }
    }

    private fun msToKmhConverter(speed: Float): Float {
        return String.format("%.2f", speed * 3.6f).toFloat()
    }

    private fun meterToKilometerConverter(distance: Double): Double {
        return String.format("%.2f", distance / 1000).toDouble()
    }

    // Starts sports tracking session when user presses start tracking-button
    private fun startTrackingSession(lm: LocationManager) {
        Log.d("click", "clicked btnStart")
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
                        progressAddress.visibility = View.VISIBLE
                        progressDistance.visibility = View.VISIBLE
                        progressSpeed.visibility = View.VISIBLE
                        progressSteps.visibility = View.VISIBLE
                        progressCalories.visibility = View.VISIBLE
                        // The numerical value of the user's weight
                        userWeightKg = userInput.text.toString().toDouble()

                        // Starts location tracking
                        lm.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            2 * 1000,
                            0f,
                            this
                        )

                        // Setting up step counter
                        sensorManager =
                            activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                        running = true

                        // Step detector works better than step counter
                        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

                        // Checks if user has step detector sensor on device
                        if (stepSensor == null) {
                            Toast.makeText(
                                activityContext,
                                "No step sensor detected on this device",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        // If the step detector exists:
                        else {
                            sensorManager?.registerListener(
                                this,
                                stepSensor,
                                SensorManager.SENSOR_DELAY_UI
                            )
                        }
                        btnStart?.visibility = View.GONE
                        btnStop?.visibility = View.VISIBLE
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

    // Ends sports tracking session when user presses stop tracking-button
    private fun endTrackingSession(lm: LocationManager) {
        Log.d("btnStop", "clicked btnStop")

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

            // Stops location tracking
            lm.removeUpdates(this)

            // Stops step detector
            running = false

            // Calculates average speed of user during session
            val averageSpeed = speedArray.average().toFloat()

            // Inserts recorded data to database: Distance, average speed, steps, calories etc...
            insertToDatabase()
            Log.d("INSERT", "DAtA INSERT")
        }

        // When user cancels popup interface
        builder.setNegativeButton(
            "keep tracking"
        ) { _, _ -> Log.d("cancel", "canceled dialog interface") }

        // Puts the popup to the screen
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // Gets current address CURRENTLY BROKEN: causes frequent crashes, currently disabled, gives "" as value
    private fun getAddress(lat: Double, lng: Double): String {
        Log.d("getAddress", "one")
        var test2 = ""

        val test3 = coroutineScope.launch {
            Log.d("getAddress", "two")
            val makeAddress = secondScope.async { makeAddress(lat, lng) }
            try {
                Log.d("getAddress", "three")
                test2 = makeAddress.await()
            } catch (e: IOException) {
                Log.d("error", "getAddress.await() error")
            }
        }
        Log.d("getAddress", "four")
        return test2
    }

    private fun makeAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(activityContext)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list[0].getAddressLine(0)
    }

    // Gets current distance traveled
    private fun getDistance(
        previousLoc: GeoPoint,
        currentLoc: GeoPoint,
        totalDistanceTraveled: Double
    ):Double {
        this.totalDistanceTraveled =
            totalDistanceTraveled + previousLoc.distanceToAsDouble(currentLoc)
        travelDistance.text =
            ("${getString(R.string.travel_distance)} ${meterToKilometerConverter(this.totalDistanceTraveled)} km")
        return totalDistanceTraveled
    }

    // Member for SensorEventListener. Is used for detecting steps
    override fun onSensorChanged(p0: SensorEvent?) {
        if (running) {
            stepCount++
            travelSteps.text = ("${getString(R.string.travel_steps)} $stepCount")
        }
    }

    // Unused, put here to stop member implementation error for SensorEventListener
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    private fun createSession(): Session? {
        return null
    }

    // Inserts recorded data to database
    private fun insertToDatabase() {
        val svm: SessionViewModel by viewModels()
        svm.insertTest()

    }

    // Counts calories from the sports session. Updates every x seconds dictated by location tracking update frequency
    private fun countCalories(
        totalDistance: Double,
        userWeight: Double
    ): Double {
        // Using CalorieCounter.kt object to reduce clutter in this fragment
        return CalorieCounter.countCalories(
            totalDistance, userWeight
        )
    }
}