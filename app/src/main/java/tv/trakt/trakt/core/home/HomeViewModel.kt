package tv.trakt.trakt.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_WELCOME_BANNER_ENABLED
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.WhatsNew
import tv.trakt.trakt.core.calendar.model.CalendarView
import tv.trakt.trakt.core.calendar.usecases.GetCalendarViewUseCase
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.HomeState.UserState
import tv.trakt.trakt.core.home.sections.monthreview.usecases.MonthReviewBannerUseCase
import tv.trakt.trakt.core.home.sections.welcome.usecases.DismissWelcomeBannerUseCase
import tv.trakt.trakt.core.home.sections.welcome.usecases.GetUserUsageUseCase
import tv.trakt.trakt.core.main.usecases.LoadWhatsNewUseCase
import java.time.YearMonth
import kotlin.time.Duration.Companion.milliseconds

private const val MONTH_REVIEW_BANNER_ID = "month-in-review"
private const val MONTH_REVIEW_LINK_SOURCE = "mir-banner"

@Suppress("UNCHECKED_CAST")
internal class HomeViewModel(
    private val dismissWelcomeUseCase: DismissWelcomeBannerUseCase,
    private val monthReviewBannerUseCase: MonthReviewBannerUseCase,
    private val getUserUsageUseCase: GetUserUsageUseCase,
    private val loadWhatsNewUseCase: LoadWhatsNewUseCase,
    private val filterManager: GlobalFilterManager,
    private val sessionManager: SessionManager,
    private val collectionStateProvider: CollectionStateProvider,
    private val getCalendarViewUseCase: GetCalendarViewUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = HomeState()
    private val initialMode = filterManager.getFilter().mode

    private val modeState = MutableStateFlow(initialMode)
    private val userState = MutableStateFlow(initialState.user)
    private val welcomeBannerState = MutableStateFlow(initialState.welcomeBanner)
    private val monthReviewState = MutableStateFlow(initialState.monthReview)
    private val whatsNewState = MutableStateFlow(initialState.whatsNew)
    private val calendarViewState = MutableStateFlow(initialState.calendarView)

    init {
        observeUser()
        observeMode()
        observeCollection()
        observeCalendarView()
        loadWhatsNew()

        analytics.logScreenView(screenName = "home")
        analytics.logMediaMode(mode = initialMode.name)
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .onEach { user ->
                loadData()
                loadMonthReview(user)
                userState.update {
                    UserState(
                        user = user,
                        loading = LoadingState.Done,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeMode() {
        filterManager.observeFilter()
            .onEach { value ->
                modeState.update { value.mode }
            }
            .launchIn(viewModelScope)
    }

    private fun observeCalendarView() {
        getCalendarViewUseCase.observeView()
            .onEach { view -> calendarViewState.update { view } }
            .launchIn(viewModelScope)
    }

    private fun observeCollection() {
        collectionStateProvider.launchIn(viewModelScope)
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                val enabled = Firebase.remoteConfig.getBoolean(MOBILE_WELCOME_BANNER_ENABLED)
                val dismissed = dismissWelcomeUseCase.isDismissed()
                if (enabled && !dismissed) {
                    val isAuthenticated = sessionManager.isAuthenticated()
                    val usage = getUserUsageUseCase.getUserUsage()
                    delay(500.milliseconds) // Delay to avoid showing the banner too quickly.
                    welcomeBannerState.update {
                        isAuthenticated && usage?.isEmpty == true
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun dismissWelcomeBanner() {
        viewModelScope.launch {
            welcomeBannerState.update { false }
            dismissWelcomeUseCase.dismissWelcomeBanner()
        }
    }

    private fun loadMonthReview(user: User?) {
        viewModelScope.launch {
            try {
                monthReviewState.update {
                    if (user?.isVip == true) {
                        monthReviewBannerUseCase.getBannerMonth(nowLocalDay())
                    } else {
                        null
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun refreshMonthReview() {
        val user = userState.value.user ?: return
        loadMonthReview(user)
    }

    fun openMonthReview(target: String) {
        analytics.logLink(
            source = MONTH_REVIEW_LINK_SOURCE,
            target = target,
        )
    }

    fun dismissMonthReview() {
        viewModelScope.launch {
            try {
                monthReviewState.update { null }
                val value = monthReviewBannerUseCase.dismissBanner(nowLocalDay())
                analytics.logBannerDismiss(
                    id = MONTH_REVIEW_BANNER_ID,
                    value = value,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
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

    fun onVipUpsellSeen() {
        analytics.vip.logUpsellImpression("home")
    }

    val state = combine(
        modeState,
        userState,
        welcomeBannerState,
        collectionStateProvider.stateFlow,
        whatsNewState,
        calendarViewState,
        monthReviewState,
    ) { state ->
        HomeState(
            mode = state[0] as MediaMode,
            user = state[1] as UserState,
            welcomeBanner = state[2] as Boolean,
            collection = state[3] as UserCollectionState,
            whatsNew = state[4] as WhatsNew?,
            calendarView = state[5] as CalendarView,
            monthReview = state[6] as YearMonth?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
