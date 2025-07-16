package tv.trakt.app.tv.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.app.tv.auth.session.SessionManager
import tv.trakt.app.tv.core.home.HomeState.AuthenticationState.AUTHENTICATED
import tv.trakt.app.tv.core.home.HomeState.AuthenticationState.LOADING
import tv.trakt.app.tv.core.home.HomeState.AuthenticationState.UNAUTHENTICATED

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
        val configUrl = Firebase.remoteConfig.getString("background_image_url")
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
