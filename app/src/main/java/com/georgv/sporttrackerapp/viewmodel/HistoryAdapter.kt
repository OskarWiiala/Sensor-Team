package com.georgv.sporttrackerapp.viewmodel

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.georgv.sporttrackerapp.R
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.database.LocationPointDao
import com.georgv.sporttrackerapp.database.SessionDB
import com.georgv.sporttrackerapp.database.SessionDao
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.*

class HistoryAdapter(private val listener: OnItemClickListener, private val context: Context) :
    ListAdapter<Session, HistoryAdapter.ViewHolder>(
        DiffCallback()
    ) {
    private val sessionDao: SessionDao = SessionDB.get(context).sessionDao()
    private val locationPointDao: LocationPointDao = SessionDB.get(context).locationPointDao()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val textView: TextView = view.findViewById(R.id.historyItem)
        val textView2: TextView = view.findViewById(R.id.hour_minute)
        val textView3: TextView = view.findViewById(R.id.duration)

        init {
            this.itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position: Int = adapterPosition
            val item = getItem(position)
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position, item.id)
            }
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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val deleteButton = viewHolder.itemView.findViewById<MaterialButton>(R.id.historyItemDelete)
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
                GlobalScope.launch {
                    locationPointDao.deleteLocationsBySession(getItem(position).id)
                    sessionDao.deleteById(getItem(position).id)
                }
            }

            // When user cancels popup interface
            builder.setNegativeButton(
                "Cancel"
            ) { _, _ -> Log.d("cancel", "canceled dialog interface") }
            // Puts the popup to the screen
            val dialog: AlertDialog = builder.create()
            dialog.show()

        }

        // Uses Day/Month/Year Hour/Minute/Second as displayed text for viewHolder
        val item = getItem(position)
        val itemDateStart = TypeConverterUtil().fromTimestamp(item.startTime)
        val itemLocalDate =
            itemDateStart!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val itemYear = itemLocalDate.year
        val itemMonth = itemLocalDate.month.toString().take(3)
        val itemDay = itemLocalDate.dayOfMonth

        val cal = Calendar.getInstance()
        cal.time = itemDateStart
        val itemHourStart = cal[Calendar.HOUR_OF_DAY]
        val itemMinuteStart = cal[Calendar.MINUTE]
        val itemSecondStart = cal[Calendar.SECOND]

        val itemDateEnd = TypeConverterUtil().fromTimestamp(item.endTime)
        val cal2 = Calendar.getInstance()
        cal2.time = itemDateEnd!!
        val itemHourEnd = cal2[Calendar.HOUR_OF_DAY]
        val itemMinuteEnd = cal2[Calendar.MINUTE]
        val itemSecondEnd = cal2[Calendar.SECOND]

        val itemDisplayDate = ("$itemDay $itemMonth $itemYear")
        val itemDisplayHourMinute = ("$itemHourStart:$itemMinuteStart")
        val itemDisplayDuration = TypeConverterUtil().durationFromHourMinuteSecond(
            itemHourStart,
            itemMinuteStart,
            itemSecondStart,
            itemHourEnd,
            itemMinuteEnd,
            itemSecondEnd
        )
        viewHolder.textView.text = itemDisplayDate
        viewHolder.textView2.text = itemDisplayHourMinute
        viewHolder.textView3.text = ("Duration: $itemDisplayDuration")

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, sessionID: Long)
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

