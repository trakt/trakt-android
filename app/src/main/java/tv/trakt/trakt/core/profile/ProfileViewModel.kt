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
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.auth.usecase.AuthorizeUserUseCase
import tv.trakt.trakt.core.auth.usecase.authCodeKey
import tv.trakt.trakt.core.profile.usecase.GetUserProfileUseCase
import tv.trakt.trakt.core.profile.usecase.LogoutUserUseCase

internal class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val authorizePreferences: DataStore<Preferences>,
    private val authorizeUseCase: AuthorizeUserUseCase,
    private val getProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUserUseCase,
) : ViewModel() {
    private val initialState = ProfileState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val profileState = MutableStateFlow(initialState.profile)
    private val signedInState = MutableStateFlow(initialState.isSignedIn)

    init {
        loadBackground()
        observeProfile()
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

    private fun observeProfile() {
        viewModelScope.launch {
            sessionManager.observeProfile()
                .collect { profile ->
                    signedInState.update { (profile != null) }
                    profileState.update { profile }
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
                loadingState.update { LOADING }

                authorizeUseCase.authorizeByCode(code)
                getProfileUseCase.loadUserProfile()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            } finally {
                loadingState.update { LoadingState.DONE }
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                logoutUseCase.logoutUser()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            } finally {
                loadingState.update { LoadingState.DONE }
            }
        }
    }

    val state: StateFlow<ProfileState> = combine(
        backgroundState,
        loadingState,
        profileState,
        signedInState,
    ) { s1, s2, s3, s4 ->
        ProfileState(
            backgroundUrl = s1,
            loading = s2,
            profile = s3,
            isSignedIn = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
