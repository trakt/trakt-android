package tv.trakt.trakt.core.users.sections.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

internal class GetUserProfileHistoryUseCase(
    private val remoteUserSource: UserHistoryRemoteDataSource,
) {
    suspend fun getUserHistory(
        userId: TraktId,
        pagination: Pagination,
        filters: GlobalFilter? = null,
    ): ImmutableList<HomeActivityItem> {
        return coroutineScope {
            val mode = filters?.mode

            val remoteEpisodesAsync =
                if (mode == null || mode.isMediaOrShows) {
                    async {
                        remoteUserSource.getEpisodesHistory(
                            userId = userId.value.toString(),
                            page = pagination.page,
                            limit = pagination.limit,
                            filters = filters,
                        )
                    }
                } else {
                    null
                }

            val remoteMoviesAsync =
                if (mode == null || mode.isMediaOrMovies) {
                    async {
                        remoteUserSource.getMoviesHistory(
                            userId = userId.value.toString(),
                            page = pagination.page,
                            limit = pagination.limit,
                            filters = filters,
                        )
                    }
                } else {
                    null
                }

            val remoteEpisodes = remoteEpisodesAsync?.await()
                ?.asyncMap {
                    HomeActivityItem.EpisodeItem(
                        id = it.id,
                        user = null,
                        userRating = null,
                        activity = it.action.value,
                        activityAt = it.watchedAt.toInstant(),
                        episode = Episode.fromDto(
                            checkNotNull(it.episode) {
                                "Episode should not be null if type is EPISODE"
                            },
                        ),
                        show = Show.fromDto(
                            checkNotNull(it.show) {
                                "Show should not be null if type is SHOW"
                            },
                        ),
                    )
                }
                ?: emptyList()

            val remoteMovies = remoteMoviesAsync?.await()
                ?.asyncMap {
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
                }
                ?: emptyList()

            return@coroutineScope (remoteEpisodes + remoteMovies)
                .sortedByDescending { it.activityAt }
                .toImmutableList()
        }
    }
}
