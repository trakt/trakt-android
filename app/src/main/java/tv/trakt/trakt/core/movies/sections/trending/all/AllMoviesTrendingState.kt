package tv.trakt.trakt.core.movies.sections.trending.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.movies.model.WatchersMovie

@Immutable
internal data class AllMoviesTrendingState(
    val items: ImmutableList<WatchersMovie>? = null,
    val loading: LoadingState = LoadingState.LOADING,
    val backgroundUrl: String? = null,
    val error: Exception? = null,
)
