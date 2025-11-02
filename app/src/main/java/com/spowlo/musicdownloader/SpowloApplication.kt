package com.spowlo.musicdownloader

import android.app.Application
import com.spowlo.musicdownloader.data.remote.RetrofitClient

/**
 * Application class for Spowlo Music Downloader
 * Initialize app-wide components here
 */
class SpowloApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Retrofit with base URL
        // For emulator: 10.0.2.2:8000 maps to localhost:8000 on the host machine
        // For physical device: use your computer's IP address (e.g., "http://192.168.1.100:8000/")
        RetrofitClient.setBaseUrl("http://10.0.2.2:8000/")
    }
}
