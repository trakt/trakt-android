package tv.trakt.trakt.core.home.sections.upnext.usecases

import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.scrobble.ScrobbleExtrasApi
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource

internal class DropPlaybackUseCase(
    private val remoteMovieSyncSource: MoviesSyncRemoteDataSource,
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
}
