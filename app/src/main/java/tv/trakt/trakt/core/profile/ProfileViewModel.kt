package tv.trakt.trakt.core.profile

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.auth.usecase.AuthorizeUserUseCase
import tv.trakt.trakt.core.auth.usecase.authCodeKey
import tv.trakt.trakt.core.profile.usecase.GetUserProfileUseCase
import tv.trakt.trakt.core.profile.usecase.LogoutUserUseCase

internal class ProfileViewModel(
    private val authorizePreferences: DataStore<Preferences>,
    private val authorizeUseCase: AuthorizeUserUseCase,
    private val getProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUserUseCase,
) : ViewModel() {
    private val initialState = ProfileState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)

    init {
        loadBackground()
        observeAuthCode()
    }

    private fun observeAuthCode() {
        viewModelScope.launch {
            authorizePreferences.data.collect { preferences ->
                preferences[authCodeKey]?.let { code ->
                    authorizePreferences.edit { it.remove(authCodeKey) }
                    authorizeUser(code)
                }
            }
        }
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString("mobile_background_image_url")
        backgroundState.update { configUrl }
    }

    private fun authorizeUser(code: String) {
        viewModelScope.launch {
            try {
                authorizeUseCase.authorizeByCode(code)
                getProfileUseCase.loadUserProfile()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                logoutUseCase.logoutUser()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            }
        }
    }

    val state: StateFlow<ProfileState> = combine(
        backgroundState,
    ) { state ->
        ProfileState(
            backgroundUrl = state[0],
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
