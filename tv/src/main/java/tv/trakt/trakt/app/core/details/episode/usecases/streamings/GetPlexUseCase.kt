package tv.trakt.trakt.app.core.details.episode.usecases.streamings

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import timber.log.Timber
import tv.trakt.trakt.common.BuildConfig
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.EpisodesSyncRemoteSource
import tv.trakt.trakt.common.data.remote.PlexRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.PLEX_PLAY_ENABLED
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import kotlin.coroutines.cancellation.CancellationException

internal class GetPlexUseCase(
    private val remoteSyncSource: ShowsSyncRemoteSource,
    private val remoteEpisodeSyncSource: EpisodesSyncRemoteSource,
    private val remoteShowSource: ShowsRemoteSource,
    private val remotePlexSource: PlexRemoteSource,
    private val localShowSource: ShowLocalSource,
) {
    suspend fun getPlexStatus(
        showId: TraktId,
        episodeId: TraktId,
    ): Result {
        var show = localShowSource.getShow(showId)

        // If we don't have the show, or it doesn't have a Plex ID, fetch details from remote and update.
        if (show == null || show.ids.plex == null) {
            show = remoteShowSource.getShowDetails(showId)
                .let { Show.fromDto(it) }
                .also { localShowSource.upsertShows(listOf(it)) }
        }

        val result = remoteSyncSource.getEpisodesPlexCollection()
        return Result(
            isPlex = result.containsKey(episodeId) && show.ids.plex != null,
            plexSlug = show.ids.plex,
        )
    }

    suspend fun getPlexStreamUrl(
        episodeTraktId: TraktId,
        showTraktId: TraktId,
    ): PlexStreamResult? {
        val isEnabled = Firebase.remoteConfig.getBoolean(PLEX_PLAY_ENABLED) || BuildConfig.DEBUG
        if (!isEnabled) {
            Timber.d("Plex play is disabled via remote config.")
            return null
        }

        return try {
            val result = remotePlexSource.getPlexStream(
                id = episodeTraktId.value.toString(),
                type = MediaType.Episode,
            )

            result.streamUrl?.let { primaryUrl ->
                val playback = runCatching {
                    remoteEpisodeSyncSource.getPlaybackProgress(
                        page = 1,
                        limit = 100,
                    )
                }.getOrNull()

                val episodeProgress = playback
                    ?.firstOrNull {
                        it.show.ids.trakt == showTraktId.value &&
                            it.episode.ids.trakt == episodeTraktId.value
                    }
                    ?.progress
                    ?: 0F

                val baseUrl = primaryUrl.substringBefore("/library/")
                PlexStreamResult(
                    primaryUrl = primaryUrl,
                    secondaryUrls = result.connections
                        ?.filter { it.uri != baseUrl }
                        ?.map { conn ->
                            // Replace the base URL in the stream URL with the connection URI.
                            primaryUrl.replace(baseUrl, conn.uri)
                        }.orEmpty(),
                    progress = episodeProgress,
                    ratingKey = result.ratingKey,
                    token = result.token,
                )
            }
        } catch (error: Exception) {
            if (error.getHttpCode() == 404) {
                Timber.w("Plex stream not found for slug: $episodeTraktId")
                return null
            }
            if (error !is CancellationException) {
                Timber.e(error, "Error fetching Plex stream for slug: ${episodeTraktId.value}")
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
        val ratingKey: String? = null,
        val token: String? = null,
    )
}
