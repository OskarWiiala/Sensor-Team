package com.georgv.sporttrackerapp.viewmodel

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.georgv.sporttrackerapp.R
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.Session

class HistoryAdapter(private val listener: OnItemClickListener, private val context: Context) : ListAdapter<Session, HistoryAdapter.ViewHolder>(
    DiffCallback()
) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),View.OnClickListener {
        val textView: TextView = view.findViewById(R.id.historyItem)

        init {
            this.itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position:Int = adapterPosition
            val item = getItem(position)
            if(position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position, item)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.history_item, viewGroup, false)

        val deleteButton = view.findViewById<Button>(R.id.historyItemDelete)

        deleteButton.setOnClickListener {
            // Creates a dialog popup interface to confirm if user wants to delete session
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setCancelable(true)
            builder.setTitle("Delete session")
            builder.setMessage("Are you sure you want delete this session?")

            // When user confirms popup interface
            builder.setPositiveButton(
                "Yes"
            ) { _, _ ->
                Log.d("confirm", "confirmed")
            }

            // When user cancels popup interface
            builder.setNegativeButton(
                "Cancel"
            ) { _, _ -> Log.d("cancel", "canceled dialog interface") }
            // Puts the popup to the screen
            val dialog: AlertDialog = builder.create()
            dialog.show()

        }
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int)  {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val item = getItem(position)
        viewHolder.textView.text = TypeConverterUtil().fromTimestamp(item.startTime).toString()
    }

    interface OnItemClickListener{
        fun onItemClick(position:Int, session: Session)
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

