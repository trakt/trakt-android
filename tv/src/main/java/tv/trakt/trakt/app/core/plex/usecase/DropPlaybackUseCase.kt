package tv.trakt.trakt.app.core.plex.usecase

import timber.log.Timber
import tv.trakt.trakt.app.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.scrobble.ScrobbleExtrasApi
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

internal class DropPlaybackUseCase(
    private val remoteMovieSyncSource: MoviesSyncRemoteDataSource,
    private val remoteEpisodeSyncSource: EpisodesSyncRemoteDataSource,
    private val remoteScrobbleSource: ScrobbleExtrasApi,
    private val cacheMarkerProvider: CacheMarkerProvider,
) {
    suspend fun dropMoviePlayback(movieId: TraktId) {
        try {
            val items = remoteMovieSyncSource.getPlaybackProgress(
                page = 1,
                limit = 100,
            )
            val playbackId = items.firstOrNull { it.movie.ids.trakt == movieId.value }?.id
            playbackId?.let {
                remoteScrobbleSource.deleteSyncPlayback(playbackId)
                cacheMarkerProvider.invalidate()
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.e(error, "Failed to get playback progress for movie ID: ${movieId.value}")
            }
        }
    }

    suspend fun dropEpisodePlayback(
        showId: TraktId,
        episodeId: TraktId,
    ) {
        try {
            val items = remoteEpisodeSyncSource.getPlaybackProgress(
                page = 1,
                limit = 100,
            )

            val playbackId = items.firstOrNull {
                it.episode.ids.trakt == episodeId.value &&
                    it.show.ids.trakt == showId.value
            }?.id

            playbackId?.let {
                remoteScrobbleSource.deleteSyncPlayback(playbackId)
                cacheMarkerProvider.invalidate()
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.e(
                    error,
                    "Failed to get playback progress for show ID: ${showId.value}, episode ID: ${episodeId.value}",
                )
            }
        }
    }
}
