package tv.trakt.trakt.core.discover.sections.releases.usecases.movies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.discover.sections.releases.data.local.movies.ReleasesMoviesLocalDataSource
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import java.time.Instant

internal class DefaultGetReleasesMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: ReleasesMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) : GetReleasesMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<CalendarItem.MovieItem> {
        return localSource.getMovies()
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(
                    it.asyncMap { item -> item.movie },
                )
            }
    }

    override suspend fun getMovies(
        startDate: Instant,
        days: Int,
        skipLocal: Boolean,
        filters: GlobalFilter,
    ): ImmutableList<CalendarItem.MovieItem> {
        return remoteSource.getReleases(
            startDate = startDate,
            days = days,
            filters = filters,
        ).asyncMap {
            CalendarItem.MovieItem(
                movie = Movie.fromDto(it.movie!!),
                watched = false,
            )
        }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localSource.setMovies(movies)
                }

                localMovieSource.upsertMovies(
                    movies.asyncMap { item -> item.movie },
                )
            }
    }
}
