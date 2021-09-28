package com.georgv.sporttrackerapp

import SessionRepository
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.georgv.sporttrackerapp.viewmodel.SessionFactory
import com.georgv.sporttrackerapp.viewmodel.SessionViewModel

class HistoryFragment : Fragment(R.layout.fragment_history) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView = view.findViewById(R.id.sessionList)
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        val cmp:SessionViewModel by viewModels()
        recyclerView.adapter = HistoryAdapter()
        cmp.sessions.observe(viewLifecycleOwner){
            (recyclerView.adapter as HistoryAdapter).submitList(it)
            Log.d("THERE IS ${it.count().toString()} SESSIONS", "TEST")
        }







    }


}