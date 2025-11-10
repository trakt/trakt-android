package tv.trakt.trakt.core.home.sections.activity.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.model.MediaMode.MEDIA
import tv.trakt.trakt.core.main.model.MediaMode.MOVIES
import tv.trakt.trakt.core.main.model.MediaMode.SHOWS
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

internal class GetPersonalActivityUseCase(
    private val remoteUserSource: UserRemoteDataSource,
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
        filter: MediaMode,
    ): ImmutableList<HomeActivityItem> {
        return coroutineScope {
            val remoteEpisodesAsync = async {
                remoteUserSource.getEpisodesHistory(page, limit)
            }
            val remoteMoviesAsync = async {
                remoteUserSource.getMoviesHistory(page, limit)
            }

            val remoteEpisodes = remoteEpisodesAsync.await()
                .asyncMap {
                    HomeActivityItem.EpisodeItem(
                        id = it.id,
                        user = null,
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

            val remoteMovies = remoteMoviesAsync.await()
                .asyncMap {
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
                }

            return@coroutineScope (remoteEpisodes + remoteMovies)
                .also {
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
                .filter {
                    when (filter) {
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
