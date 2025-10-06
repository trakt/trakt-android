package tv.trakt.trakt.core.summary.movies.features.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

internal class GetMovieHistoryUseCase(
    private val remoteSource: UserRemoteDataSource,
) {
    suspend fun getHistory(movieId: TraktId): ImmutableList<HomeActivityItem.MovieItem> {
        return remoteSource.getMovieHistory(
            movieId = movieId,
            limit = 50,
        ).asyncMap {
            HomeActivityItem.MovieItem(
                id = it.id,
                user = null,
                activity = it.action.value,
                activityAt = it.watchedAt.toInstant(),
                movie = Movie.fromDto(
                    checkNotNull(it.movie) {
                        "Movie should not be null if type is MOVIE"
                    },
                ),
            )
        }.sortedByDescending {
            it.activityAt
        }.toImmutableList()
    }
}
