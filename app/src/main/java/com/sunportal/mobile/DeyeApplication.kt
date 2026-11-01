package com.sunportal.mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Основной класс приложения
 */
@HiltAndroidApp
class DeyeApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
    }
}