package tv.trakt.trakt.app.core.home.sections.social.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.openapitools.client.models.GetUsersActivities200ResponseInner.Type.EPISODE
import org.openapitools.client.models.GetUsersActivities200ResponseInner.Type.MOVIE
import tv.trakt.trakt.common.data.remote.UserSocialRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.activity.HomeActivityItem
import tv.trakt.trakt.common.model.fromDto

internal class GetSocialActivityUseCase(
    private val remoteProfileSource: UserSocialRemoteSource,
) {
    suspend fun getSocialActivity(limit: Int): ImmutableList<HomeActivityItem> {
        val items = remoteProfileSource.getSocialActivity(
            limit = limit,
            type = "following",
            filters = null,
            extended = "full,images,streaming_ids",
        )
        return items
            .asyncMap {
                when (it.type) {
                    MOVIE -> {
                        HomeActivityItem.MovieItem(
                            id = it.id,
                            user = User.fromDto(it.user),
                            userRating = null,
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
                            userRating = null,
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
            .toImmutableList()
    }
}
