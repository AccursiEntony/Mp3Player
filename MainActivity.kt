package com.example.mp3player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.mp3player.ui.theme.Mp3PlayerTheme
import com.example.mp3player.ui.navigation.NavGraph
import android.os.Build
import android.os.Environment
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.mp3player.data.repository.PreferencesRepositoryImpl
import com.example.mp3player.data.repository.PreferencesRepository
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mp3player.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {

            val container =
                (applicationContext as Mp3PlayerApp).container

            val prefs by container
                .preferencesRepository
                .preferences
                .collectAsState(
                    initial = com.example.mp3player
                        .data.local.datastore
                        .UserPreferences()
                )

            val scope = rememberCoroutineScope()

            Mp3PlayerTheme(
                darkTheme = prefs.darkThemeEnabled
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppTheme.colors.bgMainViews
                ) {
                    NavGraph(
                        isDarkTheme = prefs.darkThemeEnabled,

                        onToggleTheme = {
                            val newValue = !prefs.darkThemeEnabled

                            scope.launch {
                                container
                                    .preferencesRepository
                                    .saveDarkTheme(newValue)
                            }
                        }
                    )
                }
            }
        }
    }
}
