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
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named
import timber.log.Timber
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_CUSTOM_THEME_ENABLED
import tv.trakt.trakt.core.auth.ConfigAuth.OAUTH_REDIRECT_URI
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.auth.usecase.authCodeKey
import tv.trakt.trakt.core.main.MainScreen
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase.CustomThemeConfig
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.colors.DarkColors
import tv.trakt.trakt.ui.theme.model.toTraktDarkColors

internal val LocalBottomBarVisibility = compositionLocalOf { mutableStateOf(true) }
internal val LocalSnackbarState = compositionLocalOf { SnackbarHostState() }

internal class MainActivity : ComponentActivity() {
    private val authPreferences: DataStore<Preferences> by inject(named(AUTH_PREFERENCES))
    private val newIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupOrientation()
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(
                scrim = Color.TRANSPARENT,
            ),
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.TRANSPARENT,
            ),
        )

        setContent {
            val bottomBarVisibility = remember { mutableStateOf(true) }
            val snackbarState = remember { SnackbarHostState() }
            val customThemeState = remember {
                getCustomThemeConfig().also {
                    customThemeConfig = it
                }
            }

            TraktTheme(
                colors = when {
                    customThemeState.enabled -> {
                        val customColors = customThemeConfig?.theme?.colors?.toTraktDarkColors()
                        customColors ?: DarkColors
                    }

                    else -> {
                        DarkColors
                    }
                },
            ) {
                CompositionLocalProvider(
                    LocalBottomBarVisibility provides bottomBarVisibility,
                    LocalSnackbarState provides snackbarState,
                ) {
                    MainScreen(
                        viewModel = koinViewModel(),
                        intent = intent,
                        newIntent = newIntent,
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
        newIntent.value = intent
        handleTraktAuthorization(intent.data)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setupOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun updateRemoteConfig() {
        with(Firebase.remoteConfig) {
            val customThemeEnabled = getBoolean(MOBILE_CUSTOM_THEME_ENABLED)
            this
                .fetchAndActivate()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Timber.d("Remote Config updated: ${it.result}")
                        if (customThemeEnabled != getBoolean(MOBILE_CUSTOM_THEME_ENABLED)) {
                            // Reload app to apply custom theme change.
                            ProcessPhoenix.triggerRebirth(this@MainActivity)
                        }
                    } else {
                        Timber.e("Remote Config update failed!")
                    }
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

    // Custom Theme

    internal var customThemeConfig: CustomThemeConfig? = null
    private val customThemeUseCase: CustomThemeUseCase by inject()

    private fun getCustomThemeConfig(): CustomThemeConfig {
        return runBlocking {
            customThemeUseCase.getConfig()
        }
    }

    internal fun toggleCustomTheme(enabled: Boolean) {
        runBlocking {
            customThemeUseCase.toggleUserEnabled(enabled)
            ProcessPhoenix.triggerRebirth(this@MainActivity)
        }
    }

    internal fun toggleCustomThemeOverlay() {
        val id = customThemeConfig?.theme?.id ?: return
        runBlocking {
            customThemeUseCase.setUserDismissedOverlay(id)
        }
    }
}
