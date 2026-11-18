package tv.trakt.trakt.app.core.profile.sections.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.common.model.SyncHistoryItem
import tv.trakt.trakt.common.data.local.EpisodeLocalSource
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.remote.UserHistoryRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

private const val HISTORY_EXTENDED = "full,streaming_ids"

internal class GetProfileHistoryUseCase(
    private val remoteUserSource: UserHistoryRemoteSource,
    private val localMoviesSource: MovieLocalSource,
    private val localEpisodesSource: EpisodeLocalSource,
) {
    suspend fun getHistory(
        page: Int = 1,
        limit: Int,
        mode: MediaMode = MediaMode.Media,
    ): ImmutableList<SyncHistoryItem> {
        return coroutineScope {
            val history = when (mode) {
                MediaMode.Media -> getMediaHistory(page, limit)
                MediaMode.Shows -> getEpisodesHistory(page, limit)
                MediaMode.Movies -> getMoviesHistory(page, limit)
            }

            return@coroutineScope history
                .also {
                    val episodes = it.mapNotNull { item -> item.episode }
                    val movies = it.mapNotNull { item -> item.movie }

                    localEpisodesSource.upsertEpisodes(episodes)
                    localMoviesSource.upsertMovies(movies)
                }
                .sortedByDescending { it.watchedAt }
                .toImmutableList()
        }
    }

    private suspend fun getMediaHistory(
        page: Int,
        limit: Int,
    ): List<SyncHistoryItem> {
        return remoteUserSource.getMediaHistory(
            page = page,
            limit = limit,
            filters = null,
            extended = HISTORY_EXTENDED,
        ).asyncMap {
            SyncHistoryItem(
                id = it.id,
                watchedAt = it.watchedAt.toZonedDateTime(),
                type = it.type.value,
                show = it.show?.let { s -> Show.fromDto(s) },
                episode = it.episode?.let { e -> Episode.fromDto(e) },
                movie = it.movie?.let { m -> Movie.fromDto(m) },
            )
        }
    }

    private suspend fun getEpisodesHistory(
        page: Int,
        limit: Int,
    ): List<SyncHistoryItem> {
        return remoteUserSource.getEpisodesHistory(
            page = page,
            limit = limit,
            filters = null,
            extended = HISTORY_EXTENDED,
        ).asyncMap {
            SyncHistoryItem(
                id = it.id,
                watchedAt = it.watchedAt.toZonedDateTime(),
                type = it.type.value,
                show = Show.fromDto(it.show),
                episode = Episode.fromDto(it.episode),
            )
        }
    }

    private suspend fun getMoviesHistory(
        page: Int,
        limit: Int,
    ): List<SyncHistoryItem> {
        return remoteUserSource.getMoviesHistory(
            page = page,
            limit = limit,
            filters = null,
            extended = HISTORY_EXTENDED,
        ).asyncMap {
            SyncHistoryItem(
                id = it.id,
                watchedAt = it.watchedAt.toZonedDateTime(),
                type = it.type.value,
                movie = Movie.fromDto(it.movie),
            )
        }
    }
}
