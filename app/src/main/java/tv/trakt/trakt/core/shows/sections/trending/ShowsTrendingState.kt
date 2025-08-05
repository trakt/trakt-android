package tv.trakt.trakt.core.shows.sections.trending

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.shows.model.WatchersShow

@Immutable
internal data class ShowsTrendingState(
    val items: ImmutableList<WatchersShow>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
