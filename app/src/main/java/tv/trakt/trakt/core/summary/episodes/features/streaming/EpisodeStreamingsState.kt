package tv.trakt.trakt.core.summary.episodes.features.streaming

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.core.streamings.model.StreamingsResult
import tv.trakt.trakt.common.helpers.LoadingState

@Immutable
internal data class EpisodeStreamingsState(
    val items: StreamingsResult? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
    val collapsed: Boolean? = null,
)
