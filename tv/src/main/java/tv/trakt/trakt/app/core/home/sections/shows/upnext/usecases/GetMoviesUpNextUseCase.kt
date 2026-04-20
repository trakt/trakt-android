package tv.trakt.trakt.app.core.home.sections.shows.upnext.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressMovie
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto

internal class GetMoviesUpNextUseCase(
    private val remoteMoviesSource: MoviesSyncRemoteDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getMoviesUpNext(
        page: Int = 1,
        limit: Int = HOME_SECTION_LIMIT,
    ): ImmutableList<ProgressMovie> {
        val remoteItems = remoteMoviesSource.getPlaybackProgress(
            limit = limit,
            page = page,
        )

        return remoteItems
            .asyncMap { item ->
                ProgressMovie(
                    movie = Movie.fromDto(item.movie),
                    progress = ProgressMovie.Progress(
                        id = item.id,
                        progress = item.progress,
                        pausedAt = item.pausedAt.toInstant(),
                    ),
                )
            }
            .toImmutableList()
            .also {
                val movies = it.asyncMap { item -> item.movie }
                localMovieSource.upsertMovies(movies)
            }
    }
}
