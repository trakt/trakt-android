package tv.trakt.trakt.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.home.HomeState.UserState
import tv.trakt.trakt.core.main.usecases.HalloweenUseCase

@OptIn(FlowPreview::class)
internal class HomeViewModel(
    private val sessionManager: SessionManager,
    private val halloweenUseCase: HalloweenUseCase,
) : ViewModel() {
    private val initialState = HomeState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val userState = MutableStateFlow(initialState.user)
    private val halloweenState = MutableStateFlow(initialState.halloween)

    init {
        loadBackground()
        loadHalloween()
        observeUser()
        observeHalloween()
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    private fun loadHalloween() {
        viewModelScope.launch {
            halloweenState.update {
                halloweenUseCase.isHalloweenEnabled()
            }
        }
    }

    private fun observeUser() {
        viewModelScope.launch {
            sessionManager.observeProfile()
                .distinctUntilChanged()
                .collect { user ->
                    userState.update {
                        UserState(
                            user = user,
                            loading = LoadingState.DONE,
                        )
                    }
                }
        }
    }

    private fun observeHalloween() {
        viewModelScope.launch {
            halloweenUseCase.observeHalloweenEnabled()
                .distinctUntilChanged()
                .collect { enabled ->
                    halloweenState.update { enabled }
                }
        }
    }

    val state: StateFlow<HomeState> = combine(
        backgroundState,
        userState,
        halloweenState,
    ) { s1, s2, s3 ->
        HomeState(
            backgroundUrl = s1,
            user = s2,
            halloween = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
