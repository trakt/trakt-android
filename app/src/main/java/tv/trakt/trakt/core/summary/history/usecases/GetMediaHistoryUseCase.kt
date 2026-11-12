package tv.trakt.trakt.core.summary.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.UserHistoryRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.activity.HomeActivityItem
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.core.summary.history.model.MediaHistoryType

internal const val HISTORY_PAGE_LIMIT = 100

internal class GetMediaHistoryUseCase(
    private val remoteSource: UserHistoryRemoteSource,
) {
    suspend fun getHistory(
        mediaType: MediaHistoryType,
        mediaId: TraktId,
        pagination: Pagination,
    ): ImmutableList<HomeActivityItem> {
        val items = when (mediaType) {
            MediaHistoryType.Movie -> getMovieHistory(mediaId, pagination)
            MediaHistoryType.Show -> getShowHistory(mediaId, pagination)
            MediaHistoryType.Episode -> getEpisodeHistory(mediaId, pagination)
        }

        return items
            .sortedByDescending { it.activityAt }
            .toImmutableList()
    }

    private suspend fun getMovieHistory(
        movieId: TraktId,
        pagination: Pagination,
    ): List<HomeActivityItem> {
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
        }
    }

    private suspend fun getShowHistory(
        showId: TraktId,
        pagination: Pagination,
    ): List<HomeActivityItem> {
        return remoteSource.getShowHistory(
            showId = showId,
            pagination = pagination,
        ).toEpisodeItems().filter {
            // Filter episodes that belong to the specified show
            it.show.ids.trakt == showId
        }
    }

    private suspend fun getEpisodeHistory(
        episodeId: TraktId,
        pagination: Pagination,
    ): List<HomeActivityItem> {
        return remoteSource.getEpisodeHistory(
            episodeId = episodeId,
            page = pagination.page,
            limit = pagination.limit,
        ).toEpisodeItems().filter {
            // Filter plays that belong to the specified episode
            it.episode.ids.trakt == episodeId
        }
    }

    private suspend fun List<SyncHistoryEpisodeItemDto>.toEpisodeItems(): List<HomeActivityItem.EpisodeItem> {
        return asyncMap {
            HomeActivityItem.EpisodeItem(
                id = it.id,
                user = null,
                userRating = null,
                activity = it.action.value,
                activityAt = it.watchedAt.toInstant(),
                episode = Episode.fromDto(
                    checkNotNull(it.episode) {
                        "Episode should not be null"
                    },
                ),
                show = Show.fromDto(
                    checkNotNull(it.show) {
                        "Show should not be null for episode history"
                    },
                ),
            )
        }
    }
}
