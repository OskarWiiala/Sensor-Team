package com.georgv.sporttrackerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter


import androidx.recyclerview.widget.RecyclerView
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.Session

class HistoryAdapter() : ListAdapter<Session, HistoryAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.historyItem)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.history_item, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int)  {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val item = getItem(position)
        viewHolder.textView.text = TypeConverterUtil().fromTimestamp(item.startTime).toString()

    }


}

class DiffCallback : DiffUtil.ItemCallback<Session>() {
    override fun areItemsTheSame(oldItem: Session, newItem: Session): Boolean {
        return oldItem == newItem
    }
    override fun areContentsTheSame(oldItem: Session, newItem: Session): Boolean {
        return oldItem == newItem
    }
}

