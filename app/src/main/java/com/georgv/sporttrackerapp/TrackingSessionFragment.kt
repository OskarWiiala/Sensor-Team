package com.georgv.sporttrackerapp

import android.Manifest
import android.app.AlertDialog
import android.content.Context
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
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.georgv.sporttrackerapp.customHandlers.Permissions
import com.google.android.material.button.MaterialButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.roundToLong

class TrackingSessionFragment : Fragment(), LocationListener {

    private var activityContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityContext = activity?.applicationContext
    }

    private var currentLatitude: Double = 0.00
    private var currentLongitude: Double = 0.00
    private var previousLoc: GeoPoint = GeoPoint(0.0, 0.0)
    private var addressValue: String = "No address"
    private var counter: Int = 0
    private var totalDistanceTraveled: Double = 0.0
    private var locationArray: MutableList<GeoPoint> = mutableListOf()

    private lateinit var mapView: MapView
    private lateinit var marker: Marker
    private lateinit var textAddress: TextView
    private lateinit var textGeo: TextView
    private lateinit var travelDistance: TextView
    private lateinit var travelSpeed: TextView

    private var btnStart: MaterialButton? = null
    private var btnStop: MaterialButton? = null

    override fun onLocationChanged(p0: Location) {
        currentLatitude = p0.latitude
        currentLongitude = p0.longitude
        addressValue = getAddress(p0.latitude, p0.longitude)

        val currentLoc = GeoPoint(currentLatitude, currentLongitude)
        if (counter == 0) {
            previousLoc = GeoPoint(currentLatitude, currentLongitude)
            locationArray.add(previousLoc)
        } else {
            previousLoc = locationArray.last()
            locationArray.add(currentLoc)
        }
        whenLocationChanged(p0)
        Log.d(
            "onLocationChanged()",
            "distance in metres: ${getDistance(previousLoc, currentLoc, totalDistanceTraveled)}"
        )
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

        btnStart = view.findViewById(R.id.btnStart)
        btnStop = view.findViewById(R.id.btnStop)
        textAddress = view.findViewById(R.id.textAddress)
        textGeo = view.findViewById(R.id.geoInfo)
        travelDistance = view.findViewById(R.id.travelDistance)
        travelSpeed = view.findViewById(R.id.travelSpeed)
        mapView = view.findViewById(R.id.mapView)

        btnStop?.visibility = View.GONE

        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as
                LocationManager

        mapView.setTileSource(TileSourceFactory.MAPNIK)

        // Adds ability to zoom with 2 fingers (multitouch)
        mapView.setMultiTouchControls(true)

        // Set zoom level with the map controller (e.g. in onCreate.
        // Avoid to change it all the time!)
        mapView.controller.setZoom(9.0)

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

        Permissions.askForPermissions("ACCESS_FINE_LOCATION", requireActivity())

        btnStart?.setOnClickListener {
            startTrackingSession(lm)
        }
        btnStop?.setOnClickListener {
            endTrackingSession(lm)
        }
    }

    private fun whenLocationChanged(loc: Location) {
        try {
            // creating GeoPoint from latitude and longitude
            val gp = GeoPoint(loc.latitude, loc.longitude)
            // Setting up mark
            marker.position = gp
            marker.title = getAddress(loc.latitude, loc.longitude)
            marker.closeInfoWindow()
            mapView.overlays.add(marker)
            mapView.invalidate()

            // Creates line between GeoPoints on map
            val line = Polyline()
            line.setPoints(locationArray)
            mapView.overlays.add(line)

            activity?.runOnUiThread() {
                // Centering map to middle of location
                mapView.controller.setCenter(gp)
                mapView.controller.animateTo(gp)
            }

            Log.d("whenLocationChanged", "speed: ${loc.speed} + hasSpeed: ${loc.hasSpeed()}")
            travelSpeed.text = ("${getString(R.string.travel_speed)} ${loc.speed} m/s")
            Log.d("", "")

            textAddress.text = ("Address: $addressValue")
            textGeo.text = ("Latitude: $currentLatitude \n Longitude: $currentLongitude")
        } catch (e: Error) {
            Log.d("whenLocationChanged()", "whenLocationChanged() error: $e")
        }
    }

    private fun startTrackingSession(lm: LocationManager) {
        Log.d("click", "clicked btnStart")
        val perms = ActivityCompat.checkSelfPermission(
            activityContext!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        when (perms) {
            0 -> {
                try {
                    Log.d("perms", "access 0")
                    lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        10 * 1000,
                        0f,
                        this
                    )
                    btnStart?.visibility = View.GONE
                    btnStop?.visibility = View.VISIBLE
                } catch (e: Error) {
                    Log.d("btnStart", "requestLocationUpdates error: $e")
                }
            }
            -1 -> Log.d("perms", "access -1")
            else -> Log.d("perms", "neither 0 or -1")
        }
    }

    private fun endTrackingSession(lm: LocationManager) {
        Log.d("btnStop", "clicked btnStop")

        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setCancelable(true)
        builder.setTitle("End session")
        builder.setMessage("Are you sure you want to end this session?")
        builder.setPositiveButton(
            "End session"
        ) { _, _ ->
            Log.d("confirm", "confirmed")
            btnStop?.visibility = View.GONE
            btnStart?.visibility = View.VISIBLE
            lm.removeUpdates(this)
        }
        builder.setNegativeButton(
            "keep tracking"
        ) { _, _ -> Log.d("cancel", "cancelled") }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun getAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(activityContext)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list[0].getAddressLine(0)
    }

    private fun getDistance(previousLoc: GeoPoint, currentLoc: GeoPoint, totalDistanceTraveled: Double) {
        this.totalDistanceTraveled = totalDistanceTraveled + previousLoc.distanceToAsDouble(currentLoc)
        travelDistance.text = ("${getString(R.string.travel_distance)} ${this.totalDistanceTraveled.roundToLong()} m")
    }
}