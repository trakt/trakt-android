package tv.trakt.trakt.core.profile

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
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_THIS_MONTH_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.thismonth.model.ThisMonthStats
import tv.trakt.trakt.core.profile.sections.thismonth.usecases.GetThisMonthUseCase
import tv.trakt.trakt.core.user.usecases.LogoutUserUseCase

internal class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val getThisMonthUseCase: GetThisMonthUseCase,
    private val logoutUseCase: LogoutUserUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ProfileState()

    private val monthBackgroundState = MutableStateFlow(initialState.monthBackgroundUrl)
    private val monthStatsState = MutableStateFlow(initialState.monthStats)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMonthStatsState = MutableStateFlow(initialState.loadingMonthStats)
    private val userState = MutableStateFlow(initialState.user)

    init {
        loadMonthBackground()
        loadData()
        observeUser()

        analytics.logScreenView(
            screenName = "profile",
        )
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

    private fun loadMonthBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_THIS_MONTH_IMAGE_URL)
        monthBackgroundState.update { configUrl }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingMonthStatsState.update { LOADING }
                monthStatsState.update {
                    getThisMonthUseCase.getThisMonthStats()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            } finally {
                loadingMonthStatsState.update { LoadingState.DONE }
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                logoutUseCase.logoutUser()
                analytics.logUserLogout()
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
        monthStatsState,
        monthBackgroundState,
        loadingState,
        loadingMonthStatsState,
        userState,
    ) { state ->
        ProfileState(
            monthStats = state[0] as ThisMonthStats?,
            monthBackgroundUrl = state[1] as String?,
            loading = state[2] as LoadingState,
            loadingMonthStats = state[3] as LoadingState,
            user = state[4] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
