package com.georgv.sporttrackerapp.customHandlers

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object Permissions {
    fun askForPermissions(perms: String?, activity: FragmentActivity) {
        if(perms == "ACCESS_FINE_LOCATION") {
            activity.let {
                Log.d("perms 0", "asking for perms")
                if (ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        it,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        0
                    )
                }
            }
        }
    }
}