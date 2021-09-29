package com.georgv.sporttrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SessionFactory(private val application: Application):
    ViewModelProvider.NewInstanceFactory() {
    override fun<T: ViewModel?> create(modelClass: Class<T>): T =
        SessionViewModel(application) as T
    }