package tv.trakt.trakt.app.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.home.HomeState.AuthenticationState.AUTHENTICATED
import tv.trakt.trakt.app.core.home.HomeState.AuthenticationState.LOADING
import tv.trakt.trakt.app.core.home.HomeState.AuthenticationState.UNAUTHENTICATED
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.data.local.MediaModeLocalSource
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.model.MediaMode

internal class HomeViewModel(
    private val sessionManager: SessionManager,
    private val mediaModeLocalSource: MediaModeLocalSource,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = HomeState()

    private val authenticationState = MutableStateFlow(initialState.authentication)
    private val modeState = MutableStateFlow(initialState.mode)
    private val profileState = MutableStateFlow(initialState.profile)
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadBackground()
        loadData()
        observeMode()
    }

    private fun observeMode() {
        mediaModeLocalSource.observeMode()
            .onEach { mode -> modeState.update { mode } }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            authenticationState.update { LOADING }

            val isAuthenticated = sessionManager.isAuthenticated()
            authenticationState.update {
                when {
                    isAuthenticated -> AUTHENTICATED
                    else -> UNAUTHENTICATED
                }
            }

            if (isAuthenticated) {
                profileState.update { sessionManager.getProfile() }
            }
        }
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    fun setMode(mode: MediaMode) {
        if (mode == modeState.value) {
            return
        }

        modeState.update { mode }
        analytics.logMediaModeClick(mode = mode.name)
        viewModelScope.launch {
            mediaModeLocalSource.setMode(mode)
        }
    }

    val state: StateFlow<HomeState> = combine(
        authenticationState,
        modeState,
        profileState,
        backgroundState,
        errorState,
    ) { s1, s2, s3, s4, s5 ->
        HomeState(
            authentication = s1,
            mode = s2,
            profile = s3,
            backgroundUrl = s4,
            error = s5,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
