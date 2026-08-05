package tv.trakt.trakt.core.summary.movies.features.streaming

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.core.streamings.model.StreamingsResult
import tv.trakt.trakt.common.helpers.LoadingState

@Immutable
internal data class MovieStreamingsState(
    val items: StreamingsResult? = null,
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
