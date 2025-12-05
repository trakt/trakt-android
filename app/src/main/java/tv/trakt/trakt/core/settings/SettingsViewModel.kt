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
import tv.trakt.trakt.core.settings.usecases.UpdateUserSettingsUseCase
import tv.trakt.trakt.core.user.usecases.LogoutUserUseCase

internal class SettingsViewModel(
    private val sessionManager: SessionManager,
    private val logoutUseCase: LogoutUserUseCase,
    private val updateSettingsUseCase: UpdateUserSettingsUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = SettingsState()

    private val userState = MutableStateFlow(initialState.user)
    private val accountLoadingState = MutableStateFlow(initialState.accountLoading)
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

    fun updateUserLocation(location: String?) {
        viewModelScope.launch {
            try {
                accountLoadingState.update { LOADING }
                updateSettingsUseCase.updateLocation(location)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                accountLoadingState.update { LoadingState.DONE }
            }
        }
    }

    fun updateUserDisplayName(displayName: String?) {
        viewModelScope.launch {
            try {
                accountLoadingState.update { LOADING }
                updateSettingsUseCase.updateDisplayName(displayName)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                accountLoadingState.update { LoadingState.DONE }
            }
        }
    }

    fun updateUserAbout(about: String?) {
        viewModelScope.launch {
            try {
                accountLoadingState.update { LOADING }
                updateSettingsUseCase.updateAbout(about)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                accountLoadingState.update { LoadingState.DONE }
            }
        }
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
        accountLoadingState,
        logoutLoadingState,
    ) { state ->
        SettingsState(
            user = state[0] as User?,
            accountLoading = state[1] as LoadingState,
            logoutLoading = state[2] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
