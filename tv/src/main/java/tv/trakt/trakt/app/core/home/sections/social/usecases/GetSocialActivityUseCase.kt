package tv.trakt.trakt.app.core.home.sections.social.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.openapitools.client.models.GetUsersActivities200ResponseInner.Type.EPISODE
import org.openapitools.client.models.GetUsersActivities200ResponseInner.Type.MOVIE
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.episodes.model.fromDto
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.sections.social.model.SocialActivityItem
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto

internal class GetSocialActivityUseCase(
    private val remoteProfileSource: ProfileRemoteDataSource,
) {
    suspend fun getSocialActivity(
        page: Int = 1,
        limit: Int = HOME_SECTION_LIMIT,
    ): ImmutableList<SocialActivityItem> {
        val items = remoteProfileSource.getUserSocialActivity(
            limit = limit,
            page = page,
            type = "following",
        )
        return items
            .asyncMap {
                when (it.type) {
                    MOVIE -> SocialActivityItem.MovieItem(
                        id = it.id,
                        user = User.fromDto(it.user),
                        activity = it.action,
                        activityAt = it.activityAt.toZonedDateTime(),
                        movie = Movie.fromDto(
                            checkNotNull(it.movie) {
                                "Movie should not be null if type is MOVIE"
                            },
                        ),
                    )
                    EPISODE -> {
                        SocialActivityItem.EpisodeItem(
                            id = it.id,
                            user = User.fromDto(it.user),
                            activity = it.action,
                            activityAt = it.activityAt.toZonedDateTime(),
                            episode = Episode.fromDto(
                                checkNotNull(it.episode) {
                                    "Episode should not be null if type is EPISODE"
                                },
                            ),
                            show = Show.fromDto(
                                checkNotNull(it.show) {
                                    "Show should not be null if type is EPISODE"
                                },
                            ),
                        )
                    }
                }
            }
            .toImmutableList()
    }
}
