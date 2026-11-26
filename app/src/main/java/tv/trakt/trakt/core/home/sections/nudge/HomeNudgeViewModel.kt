package tv.trakt.trakt.core.home.sections.nudge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
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
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.billing.analytics.VipUpsellImpressions
import tv.trakt.trakt.core.billing.model.VipUpsellSource
import tv.trakt.trakt.core.home.sections.nudge.model.VipNudge
import tv.trakt.trakt.core.home.sections.nudge.usecases.GetVipNudgeUseCase
import tv.trakt.trakt.core.home.sections.nudge.usecases.VipNudgeDismissalUseCase
import kotlin.time.Duration.Companion.milliseconds

private const val NUDGE_BANNER_ID = "vip-nudge"
private const val NUDGE_DISMISS_VALUE = "hidden"

@Suppress("UNCHECKED_CAST")
internal class HomeNudgeViewModel(
    private val getNudgeUseCase: GetVipNudgeUseCase,
    private val dismissalUseCase: VipNudgeDismissalUseCase,
    private val sessionManager: SessionManager,
    private val dataChanges: DataChanges,
    private val analytics: Analytics,
    private val upsellImpressions: VipUpsellImpressions,
) : ViewModel() {
    private val initialState = HomeNudgeState()

    private val nudgeState = MutableStateFlow(initialState.nudge)

    private var eligibleUserId: TraktId? = null
    private var isSeenReported = false
    private var loadJob: Job? = null

    init {
        observeUser()
        observeChanges()
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .map { user -> user?.takeUnless { it.isVip }?.ids?.trakt }
            .distinctUntilChanged()
            .onEach { userId ->
                eligibleUserId = userId
                loadNudge()
            }
            .launchIn(viewModelScope)
    }

    private fun observeChanges() {
        dataChanges.observe(Kind.Watchlist, Kind.Lists)
            .debounce(200.milliseconds)
            .onEach { loadNudge() }
            .launchIn(viewModelScope)
    }

    private fun loadNudge() {
        loadJob?.cancel()
        val userId = eligibleUserId
        if (userId == null) {
            nudgeState.update { null }
            return
        }

        loadJob = viewModelScope.launch {
            try {
                if (dismissalUseCase.isHidden(userId, nowUtcInstant())) {
                    nudgeState.update { null }
                    return@launch
                }
                nudgeState.update { getNudgeUseCase.getNudge() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    nudgeState.update { null }
                    Timber.recordError(error)
                }
            }
        }
    }

    fun refresh() {
        loadNudge()
    }

    fun onNudgeShown() {
        if (isSeenReported) return
        isSeenReported = true

        viewModelScope.launch {
            try {
                getNudgeUseCase.markNudgeSeen()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun onVipUpsellSeen() {
        upsellImpressions.log(VipUpsellSource.HomeNudge)
    }

    fun dismissNudge() {
        val userId = eligibleUserId ?: return
        loadJob?.cancel()
        nudgeState.update { null }

        viewModelScope.launch {
            try {
                dismissalUseCase.hide(userId, nowUtcInstant())
                analytics.logBannerDismiss(
                    id = NUDGE_BANNER_ID,
                    value = NUDGE_DISMISS_VALUE,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    val state = combine(
        nudgeState,
    ) { state ->
        HomeNudgeState(
            nudge = state[0] as VipNudge?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
