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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.inappreview.RequestAppReviewUseCase
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.errors.GlobalErrorListener
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.WhatsNew
import tv.trakt.trakt.core.auth.usecase.AuthorizeUserUseCase
import tv.trakt.trakt.core.auth.usecase.authCodeKey
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates.Source
import tv.trakt.trakt.core.checkin.model.CheckInState
import tv.trakt.trakt.core.main.usecases.DismissWelcomeUseCase
import tv.trakt.trakt.core.main.usecases.LoadWhatsNewUseCase
import tv.trakt.trakt.core.notifications.data.work.ScheduleNotificationsWorker
import tv.trakt.trakt.core.ratings.rateprompt.RatePromptManager
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState
import tv.trakt.trakt.core.user.usecases.LoadUserProfileUseCase
import tv.trakt.trakt.core.user.usecases.LogoutUserUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@OptIn(FlowPreview::class)
internal class MainViewModel(
    private val appContext: Context,
    private val sessionManager: SessionManager,
    private val checkInManager: CheckInManager,
    private val ratePromptManager: RatePromptManager,
    private val authorizePreferences: DataStore<Preferences>,
    private val authorizeUseCase: AuthorizeUserUseCase,
    private val loadWhatsNewUseCase: LoadWhatsNewUseCase,
    private val getUserUseCase: LoadUserProfileUseCase,
    private val logoutUseCase: LogoutUserUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val loadUserWatchlistUseCase: LoadUserWatchlistUseCase,
    private val loadUserListsUseCase: LoadUserListsUseCase,
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
    private val dismissWelcomeUseCase: DismissWelcomeUseCase,
    private val inAppReviewUseCase: RequestAppReviewUseCase,
    private val globalErrors: GlobalErrorListener,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = MainState()

    private val userState = MutableStateFlow(initialState.user)
    private val userVipState = MutableStateFlow(initialState.userVipStatus)
    private val checkInState = MutableStateFlow(initialState.checkIn)
    private val ratePromptState = MutableStateFlow(initialState.ratePrompt)
    private val loadingUserState = MutableStateFlow(initialState.loadingUser)
    private val welcomeState = MutableStateFlow(initialState.welcome)
    private val whatsNewState = MutableStateFlow(initialState.whatsNew)
    private val reviewState = MutableStateFlow(initialState.review)
    private val errorState = MutableStateFlow(initialState.error)

    private var lastLoadTime: Instant? = null

    init {
        loadWelcome()
        loadWhatsNew()
        loadUser()

        observeUser()
        observeAuthCode()
        observeCheckIn()
        observeRatePrompt()
        observeInAppReview()
        observeErrors()
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { user ->
                val currentUser = userState.value

                userState.update { user }
                userVipState.update { currentUser?.isVip to user?.isVip }

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

    private fun observeCheckIn() {
        checkInManager.observe()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { state ->
                checkInState.update { state }
            }
            .launchIn(viewModelScope)
    }

    private fun observeRatePrompt() {
        ratePromptManager.observe()
            .distinctUntilChanged()
            .debounce(50)
            .onEach { state ->
                ratePromptState.update { state }
            }
            .launchIn(viewModelScope)
    }

    private fun observeInAppReview() {
        inAppReviewUseCase.observeCount()
            .distinctUntilChanged()
            .filterNotNull()
            .debounce(200)
            .onEach {
                reviewState.update {
                    inAppReviewUseCase.shouldRequest()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeErrors() {
        globalErrors.observe()
            .onEach { error ->
                errorState.update { error }
            }
            .launchIn(viewModelScope)
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                delay(500)
                getUserUseCase.loadUserProfile()?.let {
                    analytics.setUserId(it.ids.trakt.value.toString())
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
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

    private fun loadWhatsNew() {
        viewModelScope.launch {
            try {
                whatsNewState.update {
                    loadWhatsNewUseCase.getWhatsNew()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun loadData() {
        if (lastLoadTime != null && nowUtcInstant().minus(1, MINUTES) < lastLoadTime) {
            Timber.d("loadData(): skipping...")
            return
        }

        viewModelScope.launch {
            Timber.d("loadData(): loading data...")
            try {
                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }

                coroutineScope {
                    val progressAsync = async { loadUserProgressUseCase.loadProgress() }
                    val watchlistAsync = async { loadUserWatchlistUseCase.loadWatchlist() }
                    val listsAsync = async { loadUserListsUseCase.loadLists() }
                    val ratingsAsync = async { loadUserRatingsUseCase.loadAll() }

                    awaitAll(
                        progressAsync,
                        watchlistAsync,
                        listsAsync,
                        ratingsAsync,
                    )
                }

                loadRatePrompt()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                lastLoadTime = nowUtcInstant()
            }
        }
    }

    fun loadNotifications(context: Context) {
        ScheduleNotificationsWorker.schedule(
            appContext = context.applicationContext,
            forceRemote = true,
        )
    }

    fun loadCheckIn() {
        viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }
                checkInManager.checkActive(appContext)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadRatePrompt() {
        viewModelScope.launch {
            try {
                ratePromptManager.checkMovies()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun authorizeUser(code: String) {
        viewModelScope.launch {
            try {
                loadingUserState.update { Loading }

                dismissOnboarding()

                authorizeUseCase.authorizeByCode(code)
                getUserUseCase.loadUserProfile()?.let {
                    analytics.setUserId(it.ids.trakt.value.toString())
                    analytics.logUserLogin()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    logoutUser()
                    Timber.recordError(error)
                }
            } finally {
                loadingUserState.update { Done }
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                loadingUserState.update { Loading }
                logoutUseCase.logoutUser()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingUserState.update { Done }
            }
        }
    }

    fun dismissWelcome() {
        viewModelScope.launch {
            welcomeState.update { it.copy(welcome = false) }
            dismissWelcomeUseCase.dismissWelcome()
        }
    }

    fun dismissCheckIn() {
        viewModelScope.launch {
            try {
                checkInManager.stop(
                    source = Source.Default,
                    context = appContext,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun dismissWhatsNew(id: Int) {
        viewModelScope.launch {
            try {
                loadWhatsNewUseCase.dismissWhatsNew(id)
                whatsNewState.update { null }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun dismissInAppReview() {
        reviewState.update { null }
    }

    private fun dismissOnboarding() {
        viewModelScope.launch {
            welcomeState.update { it.copy(onboarding = false) }
            dismissWelcomeUseCase.dismissOnboarding()
        }
    }

    fun clearError() {
        errorState.update { null }
    }

    fun clearLoadingUser() {
        loadingUserState.update { LoadingState.Idle }
    }

    fun clearRatePrompt() {
        ratePromptState.update { null }
    }

    fun suppressRatePrompt() {
        viewModelScope.launch {
            try {
                ratePromptManager.onUserSuppress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    val state = combine(
        userState,
        userVipState,
        checkInState,
        ratePromptState,
        loadingUserState,
        welcomeState,
        whatsNewState,
        reviewState,
        errorState,
    ) { state ->
        MainState(
            user = state[0] as User?,
            userVipStatus = state[1] as Pair<Boolean?, Boolean?>?,
            checkIn = state[2] as CheckInState?,
            ratePrompt = state[3] as RatePromptState?,
            loadingUser = state[4] as LoadingState,
            welcome = state[5] as MainState.WelcomeState,
            whatsNew = state[6] as WhatsNew?,
            review = state[7] as Boolean?,
            error = state[8] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
