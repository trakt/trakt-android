package tv.trakt.trakt.app.core.search.usecase.recents

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.movies.model.Movie
import tv.trakt.trakt.app.core.search.data.local.RecentSearchLocalDataSource
import tv.trakt.trakt.app.core.shows.model.Show
import tv.trakt.trakt.app.helpers.extensions.asyncMap

internal class GetRecentSearchUseCase(
    private val recentsLocalSource: RecentSearchLocalDataSource,
) {
    suspend fun getRecentShows(): ImmutableList<Show> {
        return recentsLocalSource
            .getShows()
            .sortedByDescending { it.createdAt }
            .asyncMap { it.show }
            .toImmutableList()
    }

    suspend fun getRecentMovies(): ImmutableList<Movie> {
        return recentsLocalSource
            .getMovies()
            .sortedByDescending { it.createdAt }
            .asyncMap { it.movie }
            .toImmutableList()
    }
}
