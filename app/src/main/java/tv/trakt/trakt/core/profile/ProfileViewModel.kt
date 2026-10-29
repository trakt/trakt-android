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
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_THIS_MONTH_IMAGE_URL
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.observeForegroundRefresh
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.UserStats
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.profile.sections.leaderboard.usecases.GetLeaderboardUseCase
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats
import tv.trakt.trakt.core.profile.sections.thismonth.usecases.GetProfileStatsUseCase
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val getProfileStatsUseCase: GetProfileStatsUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
    private val checkInManager: CheckInManager,
    private val filterManager: GlobalFilterManager,
    private val dataChanges: DataChanges,
    private val analytics: Analytics,
    private val appLifecycleProvider: AppLifecycleProvider,
) : ViewModel() {
    private val initialState = ProfileState()

    private val modeState = MutableStateFlow(filterManager.getFilter().mode)
    private val userState = MutableStateFlow(initialState.user)
    private val monthBackgroundState = MutableStateFlow(initialState.monthBackgroundUrl)
    private val monthStatsState = MutableStateFlow(initialState.monthStats)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMonthStatsState = MutableStateFlow(initialState.loadingMonthStats)
    private val checkInState = MutableStateFlow(initialState.checkIn)
    private val hasLeaderboardState = MutableStateFlow(initialState.hasLeaderboard)

    private var latestAllTime: UserStats? = null

    init {
        loadMonthBackground()
        loadData()
        loadLeaderboard()

        observeUser()
        observeMode()
        observeCheckIn()
        observeSocial()
        observeForeground()

        analytics.logScreenView(
            screenName = "profile",
        )
    }

    private fun observeForeground() {
        appLifecycleProvider.observeForegroundRefresh()
            .onEach {
                loadData(showLoading = false)
                loadLeaderboard()
            }
            .launchIn(viewModelScope)
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

    private fun observeMode() {
        filterManager.observeFilter()
            .map { it.mode }
            .distinctUntilChanged()
            .onEach { mode ->
                modeState.update { mode }
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

    private fun observeSocial() {
        dataChanges.observe(Kind.Social)
            .debounce(200.milliseconds)
            .onEach { loadLeaderboard() }
            .launchIn(viewModelScope)
    }

    private fun loadMonthBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_THIS_MONTH_IMAGE_URL)
        monthBackgroundState.update { configUrl }
    }

    private fun loadData(showLoading: Boolean = true) {
        loadAllTimeStats()
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingMonthStatsState.update { Loading }
                }
                val stats = getProfileStatsUseCase.getProfileStats()
                monthStatsState.update {
                    stats.copy(allTime = it?.allTime ?: latestAllTime)
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

    private fun loadAllTimeStats() {
        viewModelScope.launch {
            try {
                val allTime = getProfileStatsUseCase.getAllTimeStats()
                latestAllTime = allTime
                monthStatsState.update {
                    it?.copy(allTime = allTime)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            try {
                getLeaderboardUseCase.getLocalLeaderboard()?.let { entries ->
                    hasLeaderboardState.update { entries.isNotEmpty() }
                }

                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }

                val entries = getLeaderboardUseCase.getLeaderboard(
                    pagination = Pagination(page = 1, limit = 1),
                    saveLocal = true,
                )
                hasLeaderboardState.update { entries.isNotEmpty() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun logAllTimeStatsOpen() {
        analytics.logDrilldown(source = "stats", type = "all-time")
    }

    fun logAllTimeStatsLink(target: String) {
        analytics.logLink(source = "all-time-stats", target = target)
    }

    fun onVipUpsellSeen() {
        analytics.vip.logUpsellImpression("profile")
    }

    fun logShare() {
        analytics.logShare(source = "profile", type = "own")
    }

    val state = combine(
        monthStatsState,
        monthBackgroundState,
        loadingState,
        loadingMonthStatsState,
        userState,
        checkInState,
        hasLeaderboardState,
        modeState,
    ) { state ->
        ProfileState(
            monthStats = state[0] as ProfileStats?,
            monthBackgroundUrl = state[1] as String?,
            loading = state[2] as LoadingState,
            loadingMonthStats = state[3] as LoadingState,
            user = state[4] as User?,
            checkIn = state[5] as Boolean,
            hasLeaderboard = state[6] as Boolean,
            mode = state[7] as MediaMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
