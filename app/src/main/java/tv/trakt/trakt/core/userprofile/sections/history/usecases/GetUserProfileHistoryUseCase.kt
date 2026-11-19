package tv.trakt.trakt.core.userprofile.sections.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.translations.usecase.GetLocalizedTitlesUseCase
import tv.trakt.trakt.common.data.remote.UserHistoryRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode.Media
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.activity.HomeActivityItem
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination

internal class GetUserProfileHistoryUseCase(
    private val remoteUserSource: UserHistoryRemoteSource,
    private val getLocalizedTitlesUseCase: GetLocalizedTitlesUseCase,
) {
    suspend fun getUserHistory(
        userId: TraktId,
        pagination: Pagination,
        filters: GlobalFilter? = null,
    ): ImmutableList<HomeActivityItem> {
        val userIdParam = userId.value.toString()

        val items = when (filters?.mode) {
            null, Media -> remoteUserSource.getMediaHistory(
                userId = userIdParam,
                page = pagination.page,
                limit = pagination.limit,
                filters = filters,
            ).asyncMap(HomeActivityItem::fromDto)

            Shows -> remoteUserSource.getEpisodesHistory(
                userId = userIdParam,
                page = pagination.page,
                limit = pagination.limit,
                filters = filters,
            ).asyncMap {
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

            Movies -> remoteUserSource.getMoviesHistory(
                userId = userIdParam,
                page = pagination.page,
                limit = pagination.limit,
                filters = filters,
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
            }
        }

        return getLocalizedTitlesUseCase.localizeActivityItems(items)
            .sortedByDescending { it.activityAt }
            .toImmutableList()
    }
}
