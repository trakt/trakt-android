@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.main

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.auth.usecase.AuthorizeUserUseCase
import tv.trakt.trakt.core.auth.usecase.authCodeKey
import tv.trakt.trakt.core.main.usecases.DismissWelcomeUseCase
import tv.trakt.trakt.core.notifications.data.work.ScheduleNotificationsWorker
import tv.trakt.trakt.core.user.usecases.LoadUserProfileUseCase
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
    private val getUserUseCase: LoadUserProfileUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val loadUserWatchlistUseCase: LoadUserWatchlistUseCase,
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
    private val dismissWelcomeUseCase: DismissWelcomeUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = MainState()

    private val userState = MutableStateFlow(initialState.user)
    private val userVipState = MutableStateFlow(initialState.userVipStatus)
    private val loadingUserState = MutableStateFlow(initialState.loadingUser)
    private val welcomeState = MutableStateFlow(initialState.welcome)

    private var lastLoadTime: Instant? = null

    init {
        loadWelcome()
        loadUser()

        observeUser()
        observeAuthCode()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                delay(500)
                getUserUseCase.loadUserProfile()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            }
        }
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { user ->
                val currentUser = userState.value

                userState.update { user }
                userVipState.update {
                    currentUser?.isVip to user?.isVip
                }

                Timber.d("Observed user change: $user")
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
            val authenticatedAsync = async { sessionManager.isAuthenticated() }

            val welcomeDismissedAsync = async { dismissWelcomeUseCase.isWelcomeDismissed() }
            val onboardingDismissedAsync = async { dismissWelcomeUseCase.isOnboardingDismissed() }

            val (authenticated, welcomeDismissed, onboardingDismissed) = awaitAll(
                authenticatedAsync,
                welcomeDismissedAsync,
                onboardingDismissedAsync,
            )

            welcomeState.update {
                MainState.WelcomeState(
                    welcome = !authenticated && !welcomeDismissed,
                    onboarding = !authenticated && !onboardingDismissed,
                )
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
                    val progressAsync = async { loadUserProgressUseCase.loadProgress() }
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
                    Timber.recordError(error)
                }
            }
        }
    }

    fun loadNotifications(context: Context) {
        ScheduleNotificationsWorker.schedule(
            appContext = context.applicationContext,
            forceRemote = true,
        )
    }

    private fun authorizeUser(code: String) {
        viewModelScope.launch {
            try {
                loadingUserState.update { LOADING }

                dismissOnboarding()
                authorizeUseCase.authorizeByCode(code)
                getUserUseCase.loadUserProfile()

                analytics.logUserLogin()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    logoutUser()
                    Timber.recordError(error)
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
                    Timber.recordError(error)
                }
            } finally {
                loadingUserState.update { LoadingState.DONE }
            }
        }
    }

    fun dismissWelcome() {
        viewModelScope.launch {
            welcomeState.update { it.copy(welcome = false) }
            dismissWelcomeUseCase.dismissWelcome()
        }
    }

    private fun dismissOnboarding() {
        viewModelScope.launch {
            welcomeState.update { it.copy(onboarding = false) }
            dismissWelcomeUseCase.dismissOnboarding()
        }
    }

    val state = combine(
        userState,
        userVipState,
        loadingUserState,
        welcomeState,
    ) { state ->
        MainState(
            user = state[0] as User?,
            userVipStatus = state[1] as Pair<Boolean?, Boolean?>?,
            loadingUser = state[2] as LoadingState,
            welcome = state[3] as MainState.WelcomeState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
