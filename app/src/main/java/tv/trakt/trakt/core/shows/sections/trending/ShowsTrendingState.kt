package tv.trakt.trakt.core.shows.sections.trending

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.shows.model.TrendingShow

@Immutable
internal data class ShowsTrendingState(
    val items: ImmutableList<TrendingShow>? = null,
    val loading: Boolean = true,
    val error: Exception? = null,
)
