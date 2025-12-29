package tv.trakt.trakt.core.home.sections.activity.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.openapitools.client.models.GetUsersActivities200ResponseInner.Type.EPISODE
import org.openapitools.client.models.GetUsersActivities200ResponseInner.Type.MOVIE
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.home.sections.activity.data.local.social.HomeSocialLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.model.MediaMode.MEDIA
import tv.trakt.trakt.core.main.model.MediaMode.MOVIES
import tv.trakt.trakt.core.main.model.MediaMode.SHOWS
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

internal class GetSocialActivityUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localDataSource: HomeSocialLocalDataSource,
) {
    suspend fun getLocalSocialActivity(
        limit: Int,
        filter: MediaMode,
    ): ImmutableList<HomeActivityItem> {
        return localDataSource.getItems()
            .filter {
                when (filter) {
                    MEDIA -> true
                    MOVIES -> it is HomeActivityItem.MovieItem
                    SHOWS -> it is HomeActivityItem.EpisodeItem
                }
            }
            .sortedWith(
                compareByDescending<HomeActivityItem> { it.activityAt }
                    .thenByDescending { it.sortId },
            )
            .take(limit)
            .toImmutableList()
    }

    suspend fun getSocialActivity(
        page: Int,
        limit: Int,
        filter: MediaMode,
    ): ImmutableList<HomeActivityItem> {
        val items = remoteSource.getSocialActivity(
            limit = limit,
            type = "following",
        )
        return items
            .asyncMap {
                when (it.type) {
                    MOVIE -> {
                        HomeActivityItem.MovieItem(
                            id = it.id,
                            user = User.fromDto(it.user),
                            activity = it.action,
                            activityAt = it.activityAt.toInstant(),
                            movie = Movie.fromDto(
                                checkNotNull(it.movie) {
                                    "Movie should not be null if type is MOVIE"
                                },
                            ),
                        )
                    }

                    EPISODE -> {
                        HomeActivityItem.EpisodeItem(
                            id = it.id,
                            user = User.fromDto(it.user),
                            activity = it.action,
                            activityAt = it.activityAt.toInstant(),
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
                }
            }
            .sortedWith(
                compareByDescending<HomeActivityItem> { it.activityAt }
                    .thenByDescending { it.sortId },
            )
            .also {
                when (page) {
                    1 -> localDataSource.setItems(it)
                    else -> localDataSource.addItems(it)
                }
            }
            .filter {
                when (filter) {
                    MEDIA -> true
                    MOVIES -> it is HomeActivityItem.MovieItem
                    SHOWS -> it is HomeActivityItem.EpisodeItem
                }
            }
            .toImmutableList()
    }
}
