package tv.trakt.trakt.core.settings.categories.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.settings.data.observeSettingsProfile
import tv.trakt.trakt.core.settings.usecases.UpdateUserSettingsUseCase
import tv.trakt.trakt.core.user.usecases.LogoutUserUseCase

@Suppress("UNCHECKED_CAST")
internal class GeneralSettingsViewModel(
    private val sessionManager: SessionManager,
    private val updateSettingsUseCase: UpdateUserSettingsUseCase,
    private val logoutUseCase: LogoutUserUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = GeneralSettingsState()

    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val logoutLoadingState = MutableStateFlow(initialState.logoutLoading)

    init {
        loadUser()

        analytics.logScreenView(
            screenName = "settings_general",
        )
    }

    private fun loadUser() {
        sessionManager.observeSettingsProfile()
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    fun enableSpoilers(enable: Boolean) {
        updateSettings {
            updateSettingsUseCase.updateHideSpoilers(hidden = !enable)
            analytics.logSettings("spoilers")
        }
    }

    fun enableMultiplePlays(enable: Boolean) {
        updateSettings {
            updateSettingsUseCase.updateMultiplePlays(enable)
        }
    }

    fun enableRatePrompts(enable: Boolean) {
        updateSettings {
            updateSettingsUseCase.updateRatingsPrompt(enable)
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                logoutLoadingState.update { Loading }

                analytics.logUserLogout()
                logoutUseCase.logoutUser()

                logoutLoadingState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    logoutLoadingState.update { LoadingState.Idle }
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun updateSettings(update: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                update()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    val state = combine(
        userState,
        loadingState,
        logoutLoadingState,
    ) { state ->
        GeneralSettingsState(
            user = state[0] as User?,
            loading = state[1] as LoadingState,
            logoutLoading = state[2] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
