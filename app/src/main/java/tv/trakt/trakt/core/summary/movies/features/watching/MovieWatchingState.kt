package tv.trakt.trakt.core.summary.movies.features.watching

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState

@Immutable
internal data class MovieWatchingState(
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
