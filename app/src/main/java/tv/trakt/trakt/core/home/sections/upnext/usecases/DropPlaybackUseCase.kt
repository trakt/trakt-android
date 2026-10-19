package tv.trakt.trakt.core.home.sections.upnext.usecases

import timber.log.Timber
import tv.trakt.trakt.common.data.remote.MoviesSyncRemoteSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.api.scrobble.ScrobbleExtrasApi
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

internal class DropPlaybackUseCase(
    private val remoteMovieSyncSource: MoviesSyncRemoteSource,
    private val remoteScrobbleSource: ScrobbleExtrasApi,
    private val cacheMarkerProvider: CacheMarkerProvider,
    private val analytics: Analytics,
) {
    suspend fun dropMoviePlayback(movieId: TraktId) {
        try {
            val items = remoteMovieSyncSource.getPlaybackProgress(
                page = 1,
                limit = 100,
                filters = GlobalFilter.Default,
            )
            val playbackId = items.firstOrNull { it.movie.ids.trakt == movieId.value }?.id
            playbackId?.let {
                remoteScrobbleSource.deleteSyncPlayback(playbackId)
                cacheMarkerProvider.invalidate()
                analytics.progress.logDrop(MediaType.Movie.value)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.e(error, "Failed to get playback progress for movie ID: ${movieId.value}")
            }
        }
    }
}
