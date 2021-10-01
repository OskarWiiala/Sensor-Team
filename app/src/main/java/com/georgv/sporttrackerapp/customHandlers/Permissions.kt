package com.georgv.sporttrackerapp.customHandlers

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object Permissions {
    fun askForPermissions(perms: String?, activity: FragmentActivity) {
        if(perms == "ACCESS_FINE_LOCATION + ACTIVITY_RECOGNITION") {
            activity.let {
                Log.d("perms 0", "asking for perms: ACCESS_FINE_LOCATION")
                if (ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ActivityCompat.requestPermissions(
                            it,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION),
                            0
                        )
                    }
                }
            }
        } else {
            Log.d("Permissions.kt","Could not find permission handler for: $perms")
        }
    }
}