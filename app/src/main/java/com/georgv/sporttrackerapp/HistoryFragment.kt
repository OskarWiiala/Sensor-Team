package com.georgv.sporttrackerapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.viewmodel.HistoryAdapter

import com.georgv.sporttrackerapp.viewmodel.SessionViewModel

class HistoryFragment : Fragment(R.layout.fragment_history), HistoryAdapter.OnItemClickListener {

    private val cmp: SessionViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val historyPlaceholderTextView = view.findViewById<TextView>(R.id.history_textView)
        val recyclerView: RecyclerView = view.findViewById(R.id.sessionList)
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.adapter = this.context?.let { HistoryAdapter(this, it) }

        cmp.sessions.observe(viewLifecycleOwner) {
            (recyclerView.adapter as HistoryAdapter).submitList(it)
        }

        Log.d("HistoryFragment"," onViewCreated")
        if(recyclerView.adapter?.itemCount == 0) {
            historyPlaceholderTextView.visibility = View.VISIBLE
        } else {
            historyPlaceholderTextView.visibility = View.GONE
        }
    }

    override fun onItemClick(position: Int, sessionId: Long) {
        val activity = requireView().context as MainActivity
        activity.sendId(sessionId)
    }

    interface SendId{
        fun sendId(id:Long)
    }
}