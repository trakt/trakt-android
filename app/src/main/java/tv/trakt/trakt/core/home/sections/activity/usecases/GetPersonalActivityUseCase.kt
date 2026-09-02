package tv.trakt.trakt.core.home.sections.activity.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.openapitools.client.models.GetUsersHistoryAll200ResponseInner.Type.EPISODE
import org.openapitools.client.models.GetUsersHistoryAll200ResponseInner.Type.MOVIE
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaMode.Media
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.SyncHistoryItemDto
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

internal class GetPersonalActivityUseCase(
    private val remoteUserSource: UserHistoryRemoteDataSource,
    private val localDataSource: HomePersonalLocalDataSource,
) {
    suspend fun getLocalPersonalActivity(
        limit: Int,
        filter: MediaMode,
    ): ImmutableList<HomeActivityItem> {
        return localDataSource.getItems()
            .filter {
                when (filter) {
                    Shows -> it is HomeActivityItem.EpisodeItem
                    Movies -> it is HomeActivityItem.MovieItem
                    Media -> true
                }
            }
            .sortedByDescending { it.activityAt }
            .take(limit)
            .toImmutableList()
    }

    suspend fun getPersonalActivity(
        page: Int = 1,
        limit: Int,
        filter: GlobalFilter,
        skipLocal: Boolean = false,
    ): ImmutableList<HomeActivityItem> {
        val items = when (filter.mode) {
            Media -> remoteUserSource.getMediaHistory(page, limit, filter)
                .asyncMap { it.toActivityItem() }
            Shows -> remoteUserSource.getEpisodesHistory(page, limit, filter)
                .asyncMap {
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
            Movies -> remoteUserSource.getMoviesHistory(page, limit, filter)
                .asyncMap {
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
        }

        return items
            .also {
                if (!skipLocal) {
                    if (page == 1) {
                        localDataSource.setItems(
                            items = it,
                            notify = false,
                        )
                    } else {
                        localDataSource.addItems(
                            items = it,
                            notify = false,
                        )
                    }
                }
            }
            .sortedByDescending { it.activityAt }
            .toImmutableList()
    }

    private fun SyncHistoryItemDto.toActivityItem(): HomeActivityItem {
        return when (type) {
            EPISODE -> HomeActivityItem.EpisodeItem(
                id = id,
                user = null,
                userRating = null,
                activity = action.value,
                activityAt = watchedAt.toInstant(),
                episode = Episode.fromDto(
                    checkNotNull(episode) {
                        "Episode should not be null if type is EPISODE"
                    },
                ),
                show = Show.fromDto(
                    checkNotNull(show) {
                        "Show should not be null if type is EPISODE"
                    },
                ),
            )
            MOVIE -> HomeActivityItem.MovieItem(
                id = id,
                user = null,
                userRating = null,
                activity = action.value,
                activityAt = watchedAt.toInstant(),
                movie = Movie.fromDto(
                    checkNotNull(movie) {
                        "Movie should not be null if type is MOVIE"
                    },
                ),
            )
        }
    }
}
