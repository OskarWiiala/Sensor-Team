package com.georgv.sporttrackerapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.replace
import com.google.android.material.button.MaterialButton

class TrackerFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tracker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnNewSession = view.findViewById<MaterialButton>(R.id.btnNewSportsSession)
        btnNewSession.setOnClickListener {
            startNewTrackingSession()
        }
    }

    private fun startNewTrackingSession() {
        Log.d("TrackerFragment.kt","startNewTrackingSession() activated")
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace<TrackingSessionFragment>(R.id.fragmentContainer)
        transaction?.commit()
    }
}