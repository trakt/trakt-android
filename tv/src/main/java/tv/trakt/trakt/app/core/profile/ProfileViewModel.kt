package tv.trakt.trakt.app.core.profile

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
import tv.trakt.trakt.app.core.profile.usecases.LogoutProfileUseCase
import tv.trakt.trakt.common.auth.session.SessionManager

internal class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val logoutUseCase: LogoutProfileUseCase,
) : ViewModel() {
    private val initialState = ProfileState()

    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadBackground()
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            userState.update {
                sessionManager.getProfile()
            }
        }
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString("background_image_url")
        backgroundState.update { configUrl }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase.logoutUser()
        }
    }

    val state: StateFlow<ProfileState> = combine(
        loadingState,
        userState,
        backgroundState,
        errorState,
    ) { s1, s2, s3, s4 ->
        ProfileState(
            isLoading = s1,
            user = s2,
            backgroundUrl = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
