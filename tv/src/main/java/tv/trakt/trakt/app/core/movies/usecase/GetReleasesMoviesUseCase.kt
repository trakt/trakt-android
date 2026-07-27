package tv.trakt.trakt.app.core.movies.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

private const val DAYS_OFFSET = 1L
private const val DAYS_RANGE = 14

internal class GetMoviesReleasesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getReleases(
        limit: Int,
        range: Int = DAYS_RANGE,
    ): ImmutableList<HomeUpcomingItem.MovieItem> {
        val startDate = nowUtcInstant()
        val startDay = startDate.toLocalDay()

        val remoteItems = remoteSource.getReleases(
            startDate = startDate.minus(DAYS_OFFSET, DAYS),
            days = range,
        )

        return remoteItems
            .mapNotNull { dto ->
                val movie = dto.movie ?: return@mapNotNull null
                HomeUpcomingItem.MovieItem(
                    movie = Movie.fromDto(movie),
                )
            }
            .filter { (it.movie.released ?: LocalDate.MIN) >= startDay }
            .sortedBy { it.releaseAt }
            .take(limit)
            .toImmutableList()
            .also { items ->
                localMovieSource.upsertMovies(items.map { it.movie })
            }
    }
}
