package com.georgv.sporttrackerapp

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.roundToLong

class TrackingSessionFragment : Fragment(), LocationListener {

    private var activityContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityContext = activity?.applicationContext
    }

    private var currentLatitude: Double = 0.00
    private var currentLongitude: Double = 0.00
    private var startingLocation: GeoPoint = GeoPoint(0.0, 0.0)
    private var addressValue: String = "No address"
    private var counter: Int = 0

    private lateinit var mapView: MapView
    private lateinit var marker: Marker
    private lateinit var textAddress: TextView
    private lateinit var textGeo: TextView
    private lateinit var travelDistance: TextView
    private lateinit var travelSpeed: TextView

    override fun onLocationChanged(p0: Location) {
        currentLatitude = p0.latitude
        currentLongitude = p0.longitude
        addressValue = getAddress(p0.latitude, p0.longitude)

        val currentLoc = GeoPoint(currentLatitude, currentLongitude)
        whenLocationChanged(p0)
        if (counter == 0) {
            startingLocation = GeoPoint(currentLatitude, currentLongitude)
        }
        Log.d("onLocationChanged()", "distance in metres: ${getDistance(startingLocation, currentLoc)}")
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

        val btnStart = view.findViewById<MaterialButton>(R.id.btnStart)
        val btnStop = view.findViewById<MaterialButton>(R.id.btnStop)
        textAddress = view.findViewById(R.id.textAddress)
        textGeo = view.findViewById(R.id.geoInfo)
        travelDistance = view.findViewById(R.id.travelDistance)
        travelSpeed = view.findViewById(R.id.travelSpeed)
        mapView = view.findViewById(R.id.mapView)
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

        if ((Build.VERSION.SDK_INT >= 23 &&
                    activityContext?.let {
                        ContextCompat.checkSelfPermission(
                            it,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } !=
                    PackageManager.PERMISSION_GRANTED)
        ) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            }
        }

        btnStop.isEnabled = false
        btnStart.setOnClickListener {
            btnStart.isEnabled = false
            btnStop.isEnabled = true
            Log.d("btnStart", "clicked btnStart")
            //somewhere e.g. in "start tracking" button click listener
            try {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    this
                )
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0f,
                    this
                )
            } catch (e: Error) {
                Log.d("btnStart", "requestLocationUpdates error: $e")
            }
        }

        btnStop.setOnClickListener {
            btnStart.isEnabled = true
            btnStop.isEnabled = false
            Log.d("btnStop", "clicked btnStop")
            lm.removeUpdates(this)
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

            activity?.runOnUiThread {
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

    private fun getAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(activityContext)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list[0].getAddressLine(0)
    }

    private fun getDistance(startingLoc: GeoPoint, currentLoc: GeoPoint) {
        val distance = startingLoc.distanceToAsDouble(currentLoc)
        travelDistance.text = ("${getString(R.string.travel_distance)} ${distance.roundToLong()} m")
    }
}