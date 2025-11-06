package tv.trakt.trakt.core.main

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.auth.usecase.AuthorizeUserUseCase
import tv.trakt.trakt.core.auth.usecase.authCodeKey
import tv.trakt.trakt.core.main.usecases.DismissWelcomeUseCase
import tv.trakt.trakt.core.user.usecases.GetUserProfileUseCase
import tv.trakt.trakt.core.user.usecases.LogoutUserUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@OptIn(FlowPreview::class)
internal class MainViewModel(
    private val sessionManager: SessionManager,
    private val authorizePreferences: DataStore<Preferences>,
    private val authorizeUseCase: AuthorizeUserUseCase,
    private val getUserUseCase: GetUserProfileUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val loadUserWatchlistUseCase: LoadUserWatchlistUseCase,
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
    private val dismissWelcomeUseCase: DismissWelcomeUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = MainState()

    private val userState = MutableStateFlow(initialState.user)
    private val loadingUserState = MutableStateFlow(initialState.loadingUser)
    private val welcomeState = MutableStateFlow(initialState.welcome)

    private var lastLoadTime: Instant? = null

    init {
        loadWelcome()
        observeUser()
        observeAuthCode()
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
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

    private fun loadWelcome() {
        viewModelScope.launch {
            welcomeState.update {
                val authenticatedAsync = async { sessionManager.isAuthenticated() }
                val welcomeDismissedAsync = async { dismissWelcomeUseCase.isWelcomeDismissed() }

                val (authenticated, dismissed) = awaitAll(
                    authenticatedAsync,
                    welcomeDismissedAsync,
                )

                !authenticated && !dismissed
            }
        }
    }

    fun loadData() {
        if (lastLoadTime != null && nowUtcInstant().minus(1, MINUTES) < lastLoadTime) {
            Timber.d("Skipping...")
            return
        }

        viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }

                coroutineScope {
                    val progressAsync = async { loadUserProgressUseCase.loadAllProgress() }
                    val watchlistAsync = async { loadUserWatchlistUseCase.loadWatchlist() }
                    val ratingsAsync = async { loadUserRatingsUseCase.loadAll() }

                    awaitAll(
                        progressAsync,
                        watchlistAsync,
                        ratingsAsync,
                    )
                }

                lastLoadTime = nowUtcInstant()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            }
        }
    }

    private fun authorizeUser(code: String) {
        viewModelScope.launch {
            try {
                loadingUserState.update { LOADING }

                authorizeUseCase.authorizeByCode(code)
                getUserUseCase.loadUserProfile()

                analytics.logUserLogin()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    logoutUser()
                    Timber.e(error)
                }
            } finally {
                loadingUserState.update { LoadingState.DONE }
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                loadingUserState.update { LOADING }
                logoutUserUseCase.logoutUser()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            } finally {
                loadingUserState.update { LoadingState.DONE }
            }
        }
    }

    fun dismissWelcome() {
        viewModelScope.launch {
            welcomeState.update { false }
            dismissWelcomeUseCase.dismissWelcome()
        }
    }

    val state: StateFlow<MainState> = combine(
        userState,
        loadingUserState,
        welcomeState,
    ) { state ->
        MainState(
            user = state[0] as User?,
            loadingUser = state[1] as LoadingState,
            welcome = state[2] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
