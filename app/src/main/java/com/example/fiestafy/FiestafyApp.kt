package com.example.fiestafy

import android.app.Application
import com.example.fiestafy.utils.NotificationHelper

class FiestafyApp : Application() {
    companion object {
        lateinit var instance: FiestafyApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        NotificationHelper.init(this)
    }
} 