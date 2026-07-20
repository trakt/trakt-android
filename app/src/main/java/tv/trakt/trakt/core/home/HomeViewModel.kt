package tv.trakt.trakt.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.FlowPreview
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
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_WELCOME_BANNER_ENABLED
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.HomeState.UserState
import tv.trakt.trakt.core.home.sections.welcome.usecases.DismissWelcomeBannerUseCase
import tv.trakt.trakt.core.home.sections.welcome.usecases.GetUserUsageUseCase
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
internal class HomeViewModel(
    private val dismissWelcomeUseCase: DismissWelcomeBannerUseCase,
    private val getUserUsageUseCase: GetUserUsageUseCase,
    private val filterManager: GlobalFilterManager,
    private val sessionManager: SessionManager,
    private val collectionStateProvider: CollectionStateProvider,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = HomeState()
    private val initialMode = filterManager.getFilter().mode

    private val modeState = MutableStateFlow(initialMode)
    private val userState = MutableStateFlow(initialState.user)
    private val welcomeBannerState = MutableStateFlow(initialState.welcomeBanner)

    init {
        observeUser()
        observeMode()
        observeCollection()

        analytics.logScreenView(screenName = "home")
        analytics.logMediaMode(mode = initialMode.name)
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .onEach { user ->
                loadData()
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
                        isAuthenticated && usage.isEmpty
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

    val state = combine(
        modeState,
        userState,
        welcomeBannerState,
        collectionStateProvider.stateFlow,
    ) { s1, s2, s3, s4 ->
        HomeState(
            mode = s1,
            user = s2,
            welcomeBanner = s3,
            collection = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
