package com.georgv.sporttrackerapp.customHandlers

import android.content.Context
import androidx.core.content.ContextCompat
import com.georgv.sporttrackerapp.R
import org.osmdroid.views.overlay.advancedpolyline.ColorMapping

class PolylineColorUtil(context2: Context, speed2: Float) : ColorMapping {
    private val context = context2
    private val speed = speed2

    override fun getColorForIndex(pSegmentIndex: Int): Int {
        return when {
            speed < 1.5 -> ContextCompat.getColor(context, R.color.light_blue)
            speed >= 1.5 && speed < 3.0 -> ContextCompat.getColor(context, R.color.orange)
            speed >= 3.0 && speed < 4.0 -> ContextCompat.getColor(context, R.color.light_red)
            speed >= 4.0 && speed < 5.0 -> ContextCompat.getColor(context, R.color.red2)
            speed >= 5.0 -> ContextCompat.getColor(context, R.color.red)
            else -> ContextCompat.getColor(context, R.color.black)
        }
    }
}