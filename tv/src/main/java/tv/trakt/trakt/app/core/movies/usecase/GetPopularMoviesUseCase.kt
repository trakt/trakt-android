package tv.trakt.trakt.app.core.movies.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.movies.MoviesConfig.MOVIES_SECTION_LIMIT
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.popularYear
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter

internal class GetPopularMoviesUseCase(
    private val remoteSource: MoviesRemoteSource,
    private val localSource: MovieLocalSource,
) {
    suspend fun getPopularMovies(
        limit: Int = MOVIES_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<Movie> {
        return remoteSource.getPopular(
            page = page,
            limit = limit,
            filters = GlobalFilter.Default.copy(years = popularYear().let { it to it }),
        )
            .asyncMap {
                Movie.fromDto(it)
            }
            .toImmutableList()
            .also { movies ->
                localSource.upsertMovies(movies)
            }
    }
}
