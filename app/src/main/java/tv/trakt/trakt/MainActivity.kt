package tv.trakt.trakt

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named
import timber.log.Timber
import tv.trakt.trakt.core.auth.ConfigAuth.OAUTH_REDIRECT_URI
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.auth.usecase.authCodeKey
import tv.trakt.trakt.core.main.MainScreen
import tv.trakt.trakt.ui.theme.TraktTheme

internal val LocalBottomBarVisibility = compositionLocalOf { mutableStateOf(true) }
internal val LocalSnackbarState = compositionLocalOf { SnackbarHostState() }

internal class MainActivity : ComponentActivity() {
    private val authPreferences: DataStore<Preferences> by inject(named(AUTH_PREFERENCES))

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
            val bottomBarVisibility = remember { mutableStateOf(true) }
            val snackbarState = remember { SnackbarHostState() }

            TraktTheme {
                CompositionLocalProvider(
                    LocalBottomBarVisibility provides bottomBarVisibility,
                    LocalSnackbarState provides snackbarState,
                ) {
                    MainScreen(
                        viewModel = koinViewModel(),
                        intent = intent,
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateRemoteConfig()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleTraktAuthorization(intent.data)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setupOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun updateRemoteConfig() {
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.d("Remote Config updated: ${task.result}")
            } else {
                Timber.w("Remote Config update failed!")
            }
        }
    }

    private fun handleTraktAuthorization(authData: Uri?) {
        Timber.d("Handling Trakt authorization with data: $authData")
        if (authData.toString().startsWith(OAUTH_REDIRECT_URI)) {
            authData?.getQueryParameter("code")?.let { code ->
                runBlocking {
                    authPreferences.edit {
                        it[authCodeKey] = code
                    }
                }
            }
        }
    }
}
