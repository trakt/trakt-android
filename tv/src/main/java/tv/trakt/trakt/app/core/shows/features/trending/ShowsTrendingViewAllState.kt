package tv.trakt.trakt.app.core.shows.features.trending

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.shows.model.TrendingShow

@Immutable
internal data class ShowsTrendingViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val shows: ImmutableList<TrendingShow>? = null,
    val error: Exception? = null,
)
