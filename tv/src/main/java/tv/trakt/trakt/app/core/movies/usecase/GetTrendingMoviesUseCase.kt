package tv.trakt.trakt.app.core.movies.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.movies.MoviesConfig.MOVIES_SECTION_LIMIT
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.common.model.fromDto

internal class GetTrendingMoviesUseCase(
    private val remoteSource: MoviesRemoteSource,
    private val localSource: MovieLocalSource,
) {
    suspend fun getTrendingMovies(
        limit: Int = MOVIES_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<DiscoverItem.MovieItem> {
        return remoteSource.getTrending(
            page = page,
            limit = limit,
        )
            .asyncMap {
                DiscoverItem.MovieItem(
                    count = it.watchers,
                    movie = Movie.fromDto(it.movie),
                )
            }
            .toImmutableList()
            .also { movies ->
                localSource.upsertMovies(movies.map { it.movie })
            }
    }
}
