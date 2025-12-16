package tv.trakt.trakt.core.summary.shows.features.streaming

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.streamings.model.StreamingsResult

@Immutable
internal data class ShowStreamingsState(
    val items: StreamingsResult? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
