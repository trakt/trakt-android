package tv.trakt.trakt.core.summary.movies.features.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

internal const val HISTORY_PAGE_LIMIT = 100

internal class GetMovieHistoryUseCase(
    private val remoteSource: UserHistoryRemoteDataSource,
) {
    suspend fun getHistory(
        movieId: TraktId,
        pagination: Pagination,
    ): ImmutableList<HomeActivityItem.MovieItem> {
        return remoteSource.getMovieHistory(
            movieId = movieId,
            page = pagination.page,
            limit = pagination.limit,
        ).asyncMap {
            HomeActivityItem.MovieItem(
                id = it.id,
                user = null,
                userRating = null,
                activity = it.action.value,
                activityAt = it.watchedAt.toInstant(),
                movie = Movie.fromDto(
                    checkNotNull(it.movie) {
                        "Movie should not be null if type is MOVIE"
                    },
                ),
            )
        }.filter {
            // Filter plays that belong to the specified movie
            it.movie.ids.trakt == movieId
        }.sortedByDescending {
            it.activityAt
        }.toImmutableList()
    }
}
