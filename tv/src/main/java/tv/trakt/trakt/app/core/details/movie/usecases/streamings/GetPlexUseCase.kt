package tv.trakt.trakt.app.core.details.movie.usecases.streamings

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import timber.log.Timber
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.app.core.plex.data.PlexRemoteDataSource
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.common.BuildConfig
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.PLEX_PLAY_ENABLED
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import kotlin.coroutines.cancellation.CancellationException

internal class GetPlexUseCase(
    private val remoteSyncSource: MoviesSyncRemoteDataSource,
    private val remoteMovieSource: MoviesRemoteDataSource,
    private val remotePlexSource: PlexRemoteDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getPlexStatus(movieId: TraktId): Result {
        var movie = localMovieSource.getMovie(movieId)

        // If we don't have the movie or it doesn't have a Plex ID, fetch details from remote and update.
        if (movie == null || movie.ids.plex == null) {
            movie = remoteMovieSource.getMovieDetails(movieId)
                .let { Movie.fromDto(it) }
                .also { localMovieSource.upsertMovies(listOf(it)) }
        }

        val result = remoteSyncSource.getPlexCollection()
        return Result(
            isPlex = result.containsKey(movieId) && movie.ids.plex != null,
            plexSlug = movie.ids.plex,
        )
    }

    suspend fun getPlexStreamUrl(traktId: TraktId): PlexStreamResult? {
        val isEnabled = Firebase.remoteConfig.getBoolean(PLEX_PLAY_ENABLED) || BuildConfig.DEBUG
        if (!isEnabled) {
            Timber.d("Plex play is disabled via remote config.")
            return null
        }

        return try {
            val result = remotePlexSource.getPlexStream(
                id = traktId.value.toString(),
                type = MediaType.MOVIE,
            )
            result.streamUrl?.let { primaryUrl ->
                val playback = runCatching {
                    remoteSyncSource.getPlaybackProgress(
                        page = 1,
                        limit = 100,
                    )
                }.getOrNull()

                val movieProgress = playback
                    ?.firstOrNull { it.movie.ids.trakt == traktId.value }
                    ?.progress
                    ?: 0F

                val baseUrl = primaryUrl.substringBefore("/library/")
                PlexStreamResult(
                    primaryUrl = primaryUrl,
                    secondaryUrls = result.connections
                        ?.filter { it.uri != baseUrl }
                        ?.map { conn ->
                            // Replace the base URL in the stream URL with the conn's URI.
                            primaryUrl.replace(baseUrl, conn.uri)
                        }.orEmpty(),
                    progress = movieProgress,
                )
            }
        } catch (error: Exception) {
            if (error.getHttpCode() == 404) {
                Timber.w("Plex stream not found for slug: ${traktId.value}")
                return null
            }
            if (error !is CancellationException) {
                Timber.e(error, "Error fetching Plex stream for slug: ${traktId.value}")
            }
            throw error
        }
    }

    data class Result(
        val isPlex: Boolean,
        val plexSlug: SlugId?,
    )

    data class PlexStreamResult(
        val primaryUrl: String,
        val secondaryUrls: List<String>,
        val progress: Float,
    )
}
