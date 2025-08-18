package tv.trakt.trakt.app.core.shows

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.shows.model.AnticipatedShow
import tv.trakt.trakt.app.core.shows.model.TrendingShow
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class ShowsState(
    val isLoading: Boolean = true,
    val trendingShows: ImmutableList<TrendingShow>? = null,
    val popularShows: ImmutableList<Show>? = null,
    val anticipatedShows: ImmutableList<AnticipatedShow>? = null,
    val recommendedShows: ImmutableList<Show>? = null,
    val error: Exception? = null,
)
