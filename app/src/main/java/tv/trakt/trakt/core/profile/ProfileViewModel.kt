package tv.trakt.trakt.core.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_THIS_MONTH_IMAGE_URL
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats
import tv.trakt.trakt.core.profile.sections.thismonth.usecases.GetProfileStatsUseCase
import tv.trakt.trakt.core.user.usecases.LogoutUserUseCase
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
internal class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val getProfileStatsUseCase: GetProfileStatsUseCase,
    private val logoutUseCase: LogoutUserUseCase,
    private val checkInManager: CheckInManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ProfileState()

    private val userState = MutableStateFlow(initialState.user)
    private val monthBackgroundState = MutableStateFlow(initialState.monthBackgroundUrl)
    private val monthStatsState = MutableStateFlow(initialState.monthStats)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMonthStatsState = MutableStateFlow(initialState.loadingMonthStats)
    private val logoutLoadingState = MutableStateFlow(initialState.logoutLoading)
    private val checkInState = MutableStateFlow(initialState.checkIn)

    init {
        loadMonthBackground()
        loadData()

        observeUser()
        observeCheckIn()

        analytics.logScreenView(
            screenName = "profile",
        )
    }

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

    private fun observeCheckIn() {
        checkInManager.observe()
            .debounce(200.milliseconds)
            .map { it.isActive() }
            .distinctUntilChanged()
            .onEach { isActive ->
                checkInState.update { isActive }
            }
            .launchIn(viewModelScope)
    }

    private fun loadMonthBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_THIS_MONTH_IMAGE_URL)
        monthBackgroundState.update { configUrl }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingMonthStatsState.update { Loading }
                monthStatsState.update {
                    getProfileStatsUseCase.getProfileStats()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingMonthStatsState.update { LoadingState.Done }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                logoutLoadingState.update { Loading }

                logoutUseCase.logoutUser()
                analytics.logUserLogout()

                logoutLoadingState.update { LoadingState.Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    logoutLoadingState.update { LoadingState.Idle }
                    Timber.recordError(error)
                }
            }
        }
    }

    val state = combine(
        monthStatsState,
        monthBackgroundState,
        loadingState,
        loadingMonthStatsState,
        logoutLoadingState,
        userState,
        checkInState,
    ) { state ->
        ProfileState(
            monthStats = state[0] as ProfileStats?,
            monthBackgroundUrl = state[1] as String?,
            loading = state[2] as LoadingState,
            loadingMonthStats = state[3] as LoadingState,
            logoutLoading = state[4] as LoadingState,
            user = state[5] as User?,
            checkIn = state[6] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
