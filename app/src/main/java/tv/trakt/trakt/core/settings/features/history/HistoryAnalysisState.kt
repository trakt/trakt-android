package tv.trakt.trakt.core.settings.features.history

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.billing.model.VipUsageLimit
import tv.trakt.trakt.core.settings.features.history.model.CleanUpProgress
import tv.trakt.trakt.core.settings.features.history.model.PlayRetention
import tv.trakt.trakt.core.settings.features.history.model.PlaysSummary

@Immutable
internal data class HistoryAnalysisState(
    val retention: PlayRetention = PlayRetention.Oldest,
    val movies: PlaysSummary = PlaysSummary(),
    val episodes: PlaysSummary = PlaysSummary(),
    val loading: LoadingState = LoadingState.Idle,
    val cleanUp: CleanUpProgress? = null,
    val historyLimit: VipUsageLimit? = null,
    val historyLimitLoading: LoadingState = LoadingState.Idle,
) {
    val isCleaning: Boolean
        get() = cleanUp?.isRunning == true
}
