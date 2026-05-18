package tv.trakt.trakt.core.home.sections.activity.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaMode.MEDIA
import tv.trakt.trakt.common.model.MediaMode.MOVIES
import tv.trakt.trakt.common.model.MediaMode.SHOWS
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
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
                    SHOWS -> it is HomeActivityItem.EpisodeItem
                    MOVIES -> it is HomeActivityItem.MovieItem
                    MEDIA -> true
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
        return coroutineScope {
            val remoteEpisodesAsync = remoteUserSource.getEpisodesHistory(page, limit, filter)
            val remoteMoviesAsync = remoteUserSource.getMoviesHistory(page, limit, filter)

            val remoteEpisodes = remoteEpisodesAsync
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

            val remoteMovies = remoteMoviesAsync
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

            return@coroutineScope (remoteEpisodes + remoteMovies)
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
                .filter {
                    when (filter.mode) {
                        SHOWS -> it is HomeActivityItem.EpisodeItem
                        MOVIES -> it is HomeActivityItem.MovieItem
                        MEDIA -> true
                    }
                }
                .sortedByDescending { it.activityAt }
                .toImmutableList()
        }
    }
}
