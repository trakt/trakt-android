package tv.trakt.trakt.core.user.features.profile

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.auth.usecase.AuthorizeUserUseCase
import tv.trakt.trakt.core.auth.usecase.authCodeKey
import tv.trakt.trakt.core.user.usecase.GetUserProfileUseCase
import tv.trakt.trakt.core.user.usecase.LogoutUserUseCase

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
    private val userState = MutableStateFlow(initialState.user)

    init {
        loadBackground()
        observeUser()
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

    @OptIn(FlowPreview::class)
    private fun observeUser() {
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

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
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
                    logoutUser()
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
        userState,
    ) { s1, s2, s3 ->
        ProfileState(
            backgroundUrl = s1,
            loading = s2,
            user = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
