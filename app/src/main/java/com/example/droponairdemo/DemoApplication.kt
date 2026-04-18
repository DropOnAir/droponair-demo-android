package com.example.droponairdemo

import android.app.Application
import android.util.Log
import com.droponair.sdk.DropOnAir
import com.droponair.sdk.DropOnAirConfig
import com.example.droponairdemo.data.BackendService

/**
 * Application class, initialises DropOnAir after the user logs in.
 * (We initialise lazily in LoginActivity rather than here, because getUserJwt
 * requires the user's JWT which isn't available at app start.)
 */
class DemoApplication : Application() {
    companion object {
        lateinit var instance: DemoApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
