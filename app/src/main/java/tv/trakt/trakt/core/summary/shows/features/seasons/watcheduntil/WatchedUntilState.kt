package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class WatchedUntilState(
    val show: Show? = null,
    val episodes: ImmutableList<Episode>? = null,
    val success: Boolean? = null,
    val loading: LoadingState = Idle,
    val error: Exception? = null,
)
