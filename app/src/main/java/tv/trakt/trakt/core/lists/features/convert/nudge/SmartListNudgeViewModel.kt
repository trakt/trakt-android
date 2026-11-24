package tv.trakt.trakt.core.lists.features.convert.nudge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
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
import tv.trakt.trakt.common.model.lists.SmartListDraftsSummary
import tv.trakt.trakt.core.lists.features.convert.usecases.GetSmartListDraftsSummaryUseCase
import tv.trakt.trakt.core.lists.features.convert.usecases.SmartListDismissalsUseCase
import kotlin.time.Duration.Companion.milliseconds

private const val MIN_CONFIDENCE = 0.75F
private const val NUDGE_ID = "bar"
private const val NUDGE_DISMISSED_VALUE = "dismissed"

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class SmartListNudgeViewModel(
    private val getSummaryUseCase: GetSmartListDraftsSummaryUseCase,
    private val dismissalsUseCase: SmartListDismissalsUseCase,
    private val sessionManager: SessionManager,
    private val dataChanges: DataChanges,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = SmartListNudgeState()

    private val summaryState = MutableStateFlow(initialState.summary)

    private var dataJob: Job? = null

    init {
        loadData()
        observeListChanges()
    }

    private fun observeListChanges() {
        dataChanges.observe(Kind.Lists)
            .debounce(250.milliseconds)
            .onEach { loadData() }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                val user = sessionManager.getProfile()
                if (user == null || dismissalsUseCase.isNudgeDismissed(user.ids.trakt, nowUtcInstant())) {
                    summaryState.update { null }
                    return@launch
                }

                val summary = getSummaryUseCase.getSummary()
                summaryState.update {
                    summary.takeIf {
                        it.convertibleCount > 0 &&
                            it.lists.isNotEmpty() &&
                            (it.bestConfidence ?: 0F) >= MIN_CONFIDENCE
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun dismiss() {
        summaryState.update { null }
        viewModelScope.launch {
            try {
                val user = sessionManager.getProfile() ?: return@launch
                dismissalsUseCase.dismissNudge(user.ids.trakt, nowUtcInstant())
                analytics.logBannerDismiss(
                    id = NUDGE_ID,
                    value = NUDGE_DISMISSED_VALUE,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    val state = combine(
        summaryState,
    ) { state ->
        SmartListNudgeState(
            summary = state[0] as SmartListDraftsSummary?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
