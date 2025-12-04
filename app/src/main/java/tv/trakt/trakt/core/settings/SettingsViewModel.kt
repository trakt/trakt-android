package tv.trakt.trakt.core.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.user.usecases.LogoutUserUseCase

internal class SettingsViewModel(
    private val sessionManager: SessionManager,
    private val logoutUseCase: LogoutUserUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = SettingsState()

    private val userState = MutableStateFlow(initialState.user)
    private val logoutLoadingState = MutableStateFlow(initialState.logoutLoading)

    init {
        loadUser()

        analytics.logScreenView(
            screenName = "settings",
        )
    }

    @OptIn(FlowPreview::class)
    private fun loadUser() {
        viewModelScope.launch {
            userState.update {
                sessionManager.getProfile()
            }
        }

        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    fun logout() {
        viewModelScope.launch {
            try {
                logoutLoadingState.update { LOADING }

                logoutUseCase.logoutUser()
                analytics.logUserLogout()

                logoutLoadingState.update { LoadingState.DONE }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    logoutLoadingState.update { LoadingState.IDLE }
                    Timber.recordError(error)
                }
            }
        }
    }

    val state = combine(
        userState,
        logoutLoadingState,
    ) { state ->
        SettingsState(
            user = state[0] as User?,
            logoutLoading = state[1] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
