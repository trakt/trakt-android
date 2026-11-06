package tv.trakt.trakt.core.settings.categories.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.settings.data.observeSettingsProfile
import tv.trakt.trakt.core.settings.usecases.UpdateUserSettingsUseCase
import tv.trakt.trakt.resources.R

@Suppress("UNCHECKED_CAST")
internal class AccountSettingsViewModel(
    private val sessionManager: SessionManager,
    private val updateSettingsUseCase: UpdateUserSettingsUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = AccountSettingsState()

    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)

    init {
        loadUser()

        analytics.logScreenView(
            screenName = "settings_account",
        )
    }

    private fun loadUser() {
        sessionManager.observeSettingsProfile()
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    fun updateUserDisplayName(displayName: String?) {
        updateSettings {
            updateSettingsUseCase.updateDisplayName(displayName)
        }
    }

    fun updateUsername(username: String) {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                updateSettingsUseCase.updateUsername(username)
                analytics.logSettings("profile")
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error, "Unable to update username")
                }
                info.emit(DynamicStringResource(R.string.error_text_username))
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun updateUserLocation(location: String?) {
        updateSettings {
            updateSettingsUseCase.updateLocation(location)
        }
    }

    fun updateUserAbout(about: String?) {
        updateSettings {
            updateSettingsUseCase.updateAbout(about)
        }
    }

    fun enablePrivateAccount(enable: Boolean) {
        updateSettings {
            updateSettingsUseCase.updatePrivateAccount(enable)
        }
    }

    fun clearCoverImage() {
        updateSettings {
            updateSettingsUseCase.updateCoverImage(
                mediaId = null,
                mediaType = null,
            )
            info.emit(DynamicStringResource(R.string.text_info_cover_removed))
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

    val info: Flow<StringResource>
        field = MutableSharedFlow<StringResource>(replay = 0)

    val state = combine(
        userState,
        loadingState,
    ) { state ->
        AccountSettingsState(
            user = state[0] as User?,
            loading = state[1] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
