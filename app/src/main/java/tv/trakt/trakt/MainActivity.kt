package tv.trakt.trakt

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import tv.trakt.trakt.core.main.MainScreen
import tv.trakt.trakt.ui.theme.TraktTheme

internal class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupOrientation()
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.Companion.dark(
                scrim = Color.TRANSPARENT,
            ),
            statusBarStyle = SystemBarStyle.Companion.dark(
                scrim = Color.TRANSPARENT,
            ),
        )

        setContent {
            TraktTheme {
                MainScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateRemoteConfig()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setupOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun updateRemoteConfig() {
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MainActivity", "Remote Config updated: ${task.result}")
            } else {
                Log.w("MainActivity", "Remote Config update failed!")
            }
        }
    }
}
