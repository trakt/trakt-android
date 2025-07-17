package tv.trakt.trakt.tv.core.shows

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.tv.core.shows.model.AnticipatedShow
import tv.trakt.trakt.tv.core.shows.model.Show
import tv.trakt.trakt.tv.core.shows.model.TrendingShow

@Immutable
internal data class ShowsState(
    val isLoading: Boolean = true,
    val trendingShows: ImmutableList<TrendingShow>? = null,
    val hotShows: ImmutableList<TrendingShow>? = null,
    val popularShows: ImmutableList<Show>? = null,
    val anticipatedShows: ImmutableList<AnticipatedShow>? = null,
    val recommendedShows: ImmutableList<Show>? = null,
    val error: Exception? = null,
)
