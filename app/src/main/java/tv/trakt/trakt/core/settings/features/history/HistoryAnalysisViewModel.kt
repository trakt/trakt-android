package tv.trakt.trakt.core.settings.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.billing.model.VipUsageLimit
import tv.trakt.trakt.core.billing.usecases.GetVipUsageLimitsUseCase
import tv.trakt.trakt.core.settings.features.history.model.CleanUpProgress
import tv.trakt.trakt.core.settings.features.history.model.CleanUpTarget
import tv.trakt.trakt.core.settings.features.history.model.MediaPlays
import tv.trakt.trakt.core.settings.features.history.model.PlayRetention
import tv.trakt.trakt.core.settings.features.history.model.PlaysSummary
import tv.trakt.trakt.core.settings.features.history.usecases.CleanUpHistoryPlaysUseCase
import tv.trakt.trakt.core.settings.features.history.usecases.GetWatchedPlaysUseCase

@Suppress("UNCHECKED_CAST")
internal class HistoryAnalysisViewModel(
    private val getWatchedPlaysUseCase: GetWatchedPlaysUseCase,
    private val cleanUpHistoryPlaysUseCase: CleanUpHistoryPlaysUseCase,
    private val getUsageLimitsUseCase: GetVipUsageLimitsUseCase,
    private val dataChanges: DataChanges,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = HistoryAnalysisState()
    private val origin = ChangeOrigin.unique()

    private val retentionState = MutableStateFlow(initialState.retention)
    private val moviePlaysState = MutableStateFlow<ImmutableList<MediaPlays>>(persistentListOf())
    private val episodePlaysState = MutableStateFlow<ImmutableList<MediaPlays>>(persistentListOf())
    private val loadingState = MutableStateFlow(initialState.loading)
    private val cleanUpState = MutableStateFlow(initialState.cleanUp)
    private val historyLimitState = MutableStateFlow(initialState.historyLimit)
    private val historyLimitLoadingState = MutableStateFlow(initialState.historyLimitLoading)

    private var cleanUpJob: Job? = null

    init {
        loadPlays()
        loadHistoryLimit()

        analytics.logScreenView(
            screenName = "history_analysis",
        )
    }

    private fun loadPlays(silent: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!silent) {
                    loadingState.update { Loading }
                }

                val movies = async { getWatchedPlaysUseCase.getMoviePlays() }
                val episodes = async { getWatchedPlaysUseCase.getEpisodePlays() }

                moviePlaysState.update { movies.await() }
                episodePlaysState.update { episodes.await() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private fun loadHistoryLimit(silent: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!silent) {
                    historyLimitLoadingState.update { Loading }
                }
                historyLimitState.update { getUsageLimitsUseCase.getHistoryLimit() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                historyLimitLoadingState.update { Done }
            }
        }
    }

    fun setRetention(retention: PlayRetention) {
        if (cleanUpState.value?.isRunning == true) return
        retentionState.update { retention }
    }

    fun startCleanUp(target: CleanUpTarget) {
        if (cleanUpJob?.isActive == true) return

        val plays = when (target) {
            CleanUpTarget.Movies -> moviePlaysState.value
            CleanUpTarget.Episodes -> episodePlaysState.value
        }
        val duplicateIds = PlaysSummary.fromPlays(plays, retentionState.value).duplicateIds
        if (duplicateIds.isEmpty()) return

        cleanUpState.update {
            CleanUpProgress(
                target = target,
                total = duplicateIds.size,
            )
        }

        cleanUpJob = viewModelScope.launch {
            val failed = cleanUpHistoryPlaysUseCase.cleanUp(duplicateIds) { processed ->
                cleanUpState.update { it?.copy(processed = processed) }
            }

            cleanUpState.update { it?.copy(removed = duplicateIds.size - failed) }
            dataChanges.notify(Kind.History, origin = origin)
            loadPlays(silent = true)
            loadHistoryLimit(silent = true)
        }
    }

    fun stopCleanUp() {
        val processed = cleanUpState.value?.processed ?: 0
        cleanUpJob?.cancel()
        cleanUpJob = null
        cleanUpState.update { null }

        if (processed > 0) {
            dataChanges.notify(Kind.History, origin = origin)
        }
    }

    val state = combine(
        retentionState,
        moviePlaysState,
        episodePlaysState,
        loadingState,
        cleanUpState,
        historyLimitState,
        historyLimitLoadingState,
    ) { state ->
        val retention = state[0] as PlayRetention
        HistoryAnalysisState(
            retention = retention,
            movies = PlaysSummary.fromPlays(state[1] as ImmutableList<MediaPlays>, retention),
            episodes = PlaysSummary.fromPlays(state[2] as ImmutableList<MediaPlays>, retention),
            loading = state[3] as LoadingState,
            cleanUp = state[4] as CleanUpProgress?,
            historyLimit = state[5] as VipUsageLimit?,
            historyLimitLoading = state[6] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
