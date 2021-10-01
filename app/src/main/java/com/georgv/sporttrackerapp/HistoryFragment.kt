package com.georgv.sporttrackerapp

import SessionRepository
import android.app.blob.BlobStoreManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.viewmodel.SessionFactory
import com.georgv.sporttrackerapp.viewmodel.SessionViewModel

class HistoryFragment : Fragment(R.layout.fragment_history), HistoryAdapter.OnItemClickListener {

    private val cmp:SessionViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView = view.findViewById(R.id.sessionList)
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.adapter = HistoryAdapter(this)

        cmp.sessions.observe(viewLifecycleOwner){
            (recyclerView.adapter as HistoryAdapter).submitList(it)
        }
    }

    override fun onItemClick(position: Int, session:Session) {
        cmp.chosenSession = session
        testNavigation()
    }

    private fun testNavigation(){
        val activity = requireView().context as MainActivity
        activity.navigateToDetailView()
    }
}