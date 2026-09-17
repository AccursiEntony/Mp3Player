package com.example.mp3player

import android.app.Application
import com.example.mp3player.di.AppContainer

class Mp3PlayerApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}