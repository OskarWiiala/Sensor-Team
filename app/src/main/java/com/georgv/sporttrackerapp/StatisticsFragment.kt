package com.georgv.sporttrackerapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner

class StatisticsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    private var selectedVariable = "Distance"
    private var selectedTime = "Today"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerByVariable = view.findViewById<AppCompatSpinner>(R.id.spinnerByVariable)
        val spinnerByTime = view.findViewById<AppCompatSpinner>(R.id.spinnerByTime)

        // These lists hold the sports tracker variables (distance, steps etc...) and the time periods (day, week etc...)
        val listByVariable: MutableList<String> = ArrayList()
        val listByTime: MutableList<String> = ArrayList()

        // Since the items are predetermined they are added to the list immediately
        listByVariable.add("Distance")
        listByVariable.add("Speed")
        listByVariable.add("Steps")
        listByVariable.add("Calories")

        listByTime.add("Today")
        listByTime.add("This week")
        listByTime.add("This month")
        listByTime.add("This year")

        Log.d("onViewCreated()", "before creating adapters")

        // adapters for variables and time periods
        val adapterVariable = ArrayAdapter(
            requireActivity().applicationContext,
            R.layout.custom_spinner_item,
            listByVariable
        )
        val adapterTime = ArrayAdapter(
            requireActivity().applicationContext,
            R.layout.custom_spinner_item,
            listByTime
        )

        // Assigns the adapters
        spinnerByVariable.adapter = adapterVariable
        spinnerByTime.adapter = adapterTime

        selectByVariable(spinnerByVariable, listByVariable)
        selectByTime(spinnerByTime, listByTime)
    }

    // Selects a variable from listByVariable based on position
    private fun selectByVariable(
        spinnerByVariable: AppCompatSpinner,
        listByVariable: MutableList<String>
    ) {
        spinnerByVariable.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedVariable = listByVariable[p2]
                Log.d("spinnerByVariable click", "item selected: $selectedVariable")
            }

            // Unused, here to prevent member implementation error
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    // Selects the time period from listByTime based on position
    private fun selectByTime(
        spinnerByTime: AppCompatSpinner,
        listByTime: MutableList<String>
    ) {
        spinnerByTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedTime = listByTime[p2]
                Log.d("spinnerByTime click", "item selected: $selectedTime")
            }

            // Unused, here to prevent member implementation error
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }
}