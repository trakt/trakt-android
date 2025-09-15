package tv.trakt.trakt.app.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.home.HomeState.AuthenticationState.AUTHENTICATED
import tv.trakt.trakt.app.core.home.HomeState.AuthenticationState.LOADING
import tv.trakt.trakt.app.core.home.HomeState.AuthenticationState.UNAUTHENTICATED
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.BACKGROUND_IMAGE_URL

internal class HomeViewModel(
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = HomeState()

    private val authenticationState = MutableStateFlow(initialState.authentication)
    private val profileState = MutableStateFlow(initialState.profile)
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadBackground()
        loadData()
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

    val state: StateFlow<HomeState> = combine(
        authenticationState,
        profileState,
        backgroundState,
        errorState,
    ) { s1, s2, s3, s4 ->
        HomeState(
            authentication = s1,
            profile = s2,
            backgroundUrl = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
