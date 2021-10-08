package com.georgv.sporttrackerapp

import SessionRepository
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.viewmodel.SessionFactory
import com.georgv.sporttrackerapp.viewmodel.SessionViewModel

class SessionDetailFragment : Fragment(R.layout.fragment_session_detail){
    val svm:SessionViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val session = svm.getSessionById(0)
        //Log.d(session?.averageSpeed.toString(),"TEST")
//    val testTextView:TextView = view.findViewById(R.id.textView)
//        testTextView.text = ("callories:${session?.calories}" +
//                "\ndistance: ${session?.distance}" +
//                "\nsteps: ${session?.steps}" +
//                "\nspeed: ${session?.averageSpeed}")
//
   }


}


