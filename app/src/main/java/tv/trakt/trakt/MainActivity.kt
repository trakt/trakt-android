package tv.trakt.trakt

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.auth.AuthTabIntent
import androidx.browser.auth.AuthTabIntent.AuthResult
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named
import timber.log.Timber
import tv.trakt.trakt.app.TvSplashActivity
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_CUSTOM_THEME_ENABLED
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HTTPS_AUTH_CALLBACK_ENABLED
import tv.trakt.trakt.common.helpers.extensions.isTelevision
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.ui.theme.colors.DarkColors
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.core.auth.AuthBrowser
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.core.auth.ConfigAuth.OAUTH_HTTPS_REDIRECT_URI
import tv.trakt.trakt.core.auth.ConfigAuth.OAUTH_REDIRECT_SCHEME
import tv.trakt.trakt.core.auth.ConfigAuth.OAUTH_REDIRECT_URI
import tv.trakt.trakt.core.auth.Pkce
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.auth.usecase.authCodeKey
import tv.trakt.trakt.core.auth.usecase.authRedirectUriKey
import tv.trakt.trakt.core.auth.usecase.codeVerifierKey
import tv.trakt.trakt.core.auth.usecase.forceLoginKey
import tv.trakt.trakt.core.main.MainScreen
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase.CustomThemeConfig
import tv.trakt.trakt.core.settings.data.ThemeModeCache
import tv.trakt.trakt.core.settings.usecases.ThemeModeUseCase
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.model.ThemeMode
import tv.trakt.trakt.ui.theme.model.toTraktDarkColors

internal val LocalBottomBarVisibility = compositionLocalOf { mutableStateOf(true) }
internal val LocalCheckInVisibility = compositionLocalOf { mutableStateOf(true) }
internal val LocalRatePromptVisibility = compositionLocalOf { mutableStateOf(true) }
internal val LocalSnackbarState = compositionLocalOf { SnackbarHostState() }

internal val LocalSnackbarBottomOffset = compositionLocalOf { mutableStateOf(0.dp) }
internal val LocalStartAuthorization = staticCompositionLocalOf { {} }

internal class MainActivity : AppCompatActivity() {
    private val authPreferences: DataStore<Preferences> by lazy {
        inject<DataStore<Preferences>>(named(AUTH_PREFERENCES)).value
    }

    private val themeModeUseCase: ThemeModeUseCase by lazy {
        inject<ThemeModeUseCase>().value
    }

    private val themeModeCache: ThemeModeCache by lazy {
        inject<ThemeModeCache>().value
    }

    private val authBrowser = AuthBrowser(
        activity = this,
        onAuthTabResult = ::handleAuthTabResult,
    )

    private val newIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isTelevision()) {
            startActivity(
                Intent(this, TvSplashActivity::class.java),
            )
            finish()
            return
        }

        setupOrientation()
        setupWindowBackground(mode = themeModeCache.read() ?: ThemeMode.Default)
        handleTraktAuthorization(intent)

        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(
                scrim = Color.TRANSPARENT,
            ),
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.TRANSPARENT,
            ),
        )

        setContent {
            val themeMode by themeModeUseCase.observeThemeMode()
                .collectAsStateWithLifecycle(
                    initialValue = themeModeCache.read() ?: ThemeMode.Default,
                )

            val darkTheme = when (themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            LaunchedEffect(darkTheme) {
                window.setBackgroundDrawable(
                    when {
                        darkTheme -> DarkColors.backgroundPrimary
                        else -> LightColors.backgroundPrimary
                    }.toArgb().toDrawable(),
                )

                enableEdgeToEdge(
                    navigationBarStyle = when {
                        darkTheme -> SystemBarStyle.dark(scrim = Color.TRANSPARENT)
                        else -> SystemBarStyle.light(
                            scrim = Color.TRANSPARENT,
                            darkScrim = Color.TRANSPARENT,
                        )
                    },
                    statusBarStyle = when {
                        darkTheme -> SystemBarStyle.dark(scrim = Color.TRANSPARENT)
                        else -> SystemBarStyle.light(
                            scrim = Color.TRANSPARENT,
                            darkScrim = Color.TRANSPARENT,
                        )
                    },
                )
            }

            val bottomBarVisibility = remember { mutableStateOf(true) }
            val checkInVisibility = remember { mutableStateOf(true) }
            val ratePromptVisibility = remember { mutableStateOf(true) }
            val snackbarBottomOffset = remember { mutableStateOf(0.dp) }
            val snackbarState = remember { SnackbarHostState() }
            val customThemeState = remember {
                getCustomThemeConfig().also {
                    customThemeConfig = it
                }
            }

            val startAuthorization = remember {
                { startAuthorization() }
            }

            TraktTheme(
                colors = when {
                    darkTheme && customThemeState.enabled -> {
                        val customColors = customThemeConfig?.theme?.colors?.toTraktDarkColors()
                        customColors ?: DarkColors
                    }
                    darkTheme -> {
                        DarkColors
                    }
                    else -> {
                        LightColors
                    }
                },
            ) {
                CompositionLocalProvider(
                    LocalBottomBarVisibility provides bottomBarVisibility,
                    LocalCheckInVisibility provides checkInVisibility,
                    LocalRatePromptVisibility provides ratePromptVisibility,
                    LocalSnackbarState provides snackbarState,
                    LocalSnackbarBottomOffset provides snackbarBottomOffset,
                    LocalStartAuthorization provides startAuthorization,
                ) {
                    MainScreen(
                        viewModel = koinViewModel(),
                        appLinkViewModel = koinViewModel(),
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
        handleTraktAuthorization(intent)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setupOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /**
     * Paints the window before the first frame so the launch does not flash the wrong colour.
     */
    private fun setupWindowBackground(mode: ThemeMode) {
        val uiMode = resources.configuration.uiMode
        val isNightMode = (uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES

        val darkTheme = when (mode) {
            ThemeMode.System -> isNightMode
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }

        window.setBackgroundDrawable(
            when {
                darkTheme -> DarkColors.backgroundPrimary
                else -> LightColors.backgroundPrimary
            }.toArgb().toDrawable(),
        )
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

    private fun startAuthorization(allowHttpsRedirect: Boolean = true) {
        lifecycleScope.launch {
            val redirectUri = authRedirectUri(
                httpsSupported = allowHttpsRedirect && authBrowser.supportsAuthTab(),
            )
            val forceLogin = authPreferences.data.first()[forceLoginKey] == true
            val codeVerifier = Pkce.generateCodeVerifier()
            authPreferences.edit { it[codeVerifierKey] = codeVerifier }
            authBrowser.open(
                url = ConfigAuth.authCodeUrl(
                    codeVerifier = codeVerifier,
                    redirectUri = redirectUri,
                    forceLogin = forceLogin,
                ).toUri(),
                redirectUri = redirectUri,
            )
        }
    }

    /**
     * The https redirect is only used through an Auth Tab. A regular browser often fails to hand
     * the final redirect off to the app, leaving the user on the web handoff page.
     */
    private fun authRedirectUri(httpsSupported: Boolean): String {
        return when {
            httpsSupported && Firebase.remoteConfig.getBoolean(MOBILE_HTTPS_AUTH_CALLBACK_ENABLED) -> {
                OAUTH_HTTPS_REDIRECT_URI
            }
            else -> {
                OAUTH_REDIRECT_URI
            }
        }.also {
            Timber.d("Using Trakt auth redirect URI: $it")
        }
    }

    private fun handleAuthTabResult(result: AuthResult) {
        when (result.resultCode) {
            AuthTabIntent.RESULT_OK -> {
                result.resultUri?.let(::storeAuthorizationCode)
            }
            AuthTabIntent.RESULT_VERIFICATION_FAILED,
            AuthTabIntent.RESULT_VERIFICATION_TIMED_OUT,
            -> {
                Timber.w("Auth Tab could not verify the https redirect (%d), retrying", result.resultCode)
                startAuthorization(allowHttpsRedirect = false)
            }
            else -> {
                Timber.d("Auth Tab closed without a redirect (%d)", result.resultCode)
            }
        }
    }

    private fun handleTraktAuthorization(intent: Intent?) {
        val authData = intent?.data ?: return
        if (storeAuthorizationCode(authData)) {
            // Consume the intent so a configuration change does not replay the same code.
            intent.data = null
        }
    }

    private fun storeAuthorizationCode(authData: Uri): Boolean {
        val redirectUri = when {
            ConfigAuth.isHttpsRedirect(authData) -> OAUTH_HTTPS_REDIRECT_URI
            authData.scheme == OAUTH_REDIRECT_SCHEME -> OAUTH_REDIRECT_URI
            else -> return false
        }

        Timber.d("Handling Trakt authorization with data: %s", authData)
        if (!authData.toString().startsWith(redirectUri)) {
            Timber.recordError(
                IllegalArgumentException("Invalid Trakt authorization data: $authData"),
            )
            return false
        }

        val code = authData.getQueryParameter("code")
        if (code.isNullOrBlank()) {
            Timber.recordError(
                IllegalArgumentException("Trakt authorization redirect is missing code: $authData"),
            )
            return false
        }

        runBlocking {
            authPreferences.edit {
                it[authCodeKey] = code
                it[authRedirectUriKey] = redirectUri
            }
        }
        return true
    }

    // Custom Theme
    internal var customThemeConfig: CustomThemeConfig? = null
    private val customThemeUseCase: CustomThemeUseCase by lazy {
        inject<CustomThemeUseCase>().value
    }

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
