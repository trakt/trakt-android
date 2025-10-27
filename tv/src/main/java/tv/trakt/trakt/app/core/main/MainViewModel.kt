package tv.trakt.trakt.app.core.main

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.auth.usecases.LoadUserProfileUseCase
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation

private val KEY_SHOW_SPLASH = booleanPreferencesKey("key_show_splash")

internal class MainViewModel(
    private val sessionManager: SessionManager,
    private val mainDataStore: DataStore<Preferences>,
    private val loadUserProfileUseCase: LoadUserProfileUseCase,
) : ViewModel() {
    private val initialState = MainState()

    private val splashState = MutableStateFlow(initialState.splash)
    private val profileState = MutableStateFlow(initialState.profile)
    private val signedOutState = MutableStateFlow(initialState.isSignedOut)

    init {
        observeProfile()
        loadProfile()
        loadSplashScreen()
    }

    private fun loadSplashScreen() {
        viewModelScope.launch {
            val data = mainDataStore.data.first()

            val splashShown = data[KEY_SHOW_SPLASH] ?: false
            mainDataStore.edit {
                it[KEY_SHOW_SPLASH] = true
            }

            splashState.update { !splashShown }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            sessionManager.observeProfile()
                .collect { profile ->
                    val isSignedOut = profile == null && profileState.value != null
                    profileState.update { profile }
                    signedOutState.update { isSignedOut }
                }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                loadUserProfileUseCase.loadUserProfile()
            } catch (error: Exception) {
                error.rethrowCancellation()
                Timber.w(error, "Failed to load user profile")
            }
        }
    }

    fun dismissSplash() {
        splashState.update { false }
        Timber.d("Splash screen dismissed")
    }

    val state: StateFlow<MainState> = combine(
        splashState,
        profileState,
        signedOutState,
    ) { s1, s2, s3 ->
        MainState(
            splash = s1,
            profile = s2,
            isSignedOut = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
