package tv.trakt.trakt.app.core.details.episode.usecases.streamings

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import timber.log.Timber
import tv.trakt.trakt.app.core.plex.data.PlexRemoteDataSource
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.PLEX_PLAY_ENABLED
import tv.trakt.trakt.common.helpers.extensions.getHttpErrorCode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetPlexUseCase(
    private val remoteSyncSource: ShowsSyncRemoteDataSource,
    private val remoteShowSource: ShowsRemoteDataSource,
    private val remotePlexSource: PlexRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
) {
    suspend fun getPlexStatus(
        showId: TraktId,
        episodeId: TraktId,
    ): Result {
        var show = localShowSource.getShow(showId)

        // If we don't have the show, or it doesn't have a Plex ID, fetch details from remote and update.
        if (show == null || show.ids.plex == null) {
            show = remoteShowSource.getShowDetails(showId)
                ?.let { Show.fromDto(it) }
                ?.also { localShowSource.upsertShows(listOf(it)) }
        }

        val result = remoteSyncSource.getEpisodesPlexCollection()
        return Result(
            isPlex = result.containsKey(episodeId) && show?.ids?.plex != null,
            plexSlug = show?.ids?.plex,
        )
    }

    suspend fun getPlexStreamUrl(traktId: TraktId): PlexStreamResult? {
        val isEnabled = Firebase.remoteConfig.getBoolean(PLEX_PLAY_ENABLED)
        if (!isEnabled) {
            Timber.d("Plex play is disabled via remote config.")
            return null
        }

        return try {
            val result = remotePlexSource.getPlexStream(
                id = traktId.value.toString(),
                type = MediaType.EPISODE,
            )
            result.streamUrl?.let { primaryUrl ->
                val baseUrl = primaryUrl.substringBefore("/library/")
                PlexStreamResult(
                    primaryUrl = primaryUrl,
                    secondaryUrls = result.connections
                        ?.filter { it.uri != baseUrl }
                        ?.map { conn ->
                            // Replace the base URL in the stream URL with the conn's URI.
                            primaryUrl.replace(baseUrl, conn.uri)
                        }.orEmpty(),
                )
            }
        } catch (error: Exception) {
            if (error.getHttpErrorCode() == 404) {
                Timber.w("Plex stream not found for slug: $traktId")
                return null
            }
            Timber.e(error, "Error fetching Plex stream for slug: ${traktId.value}")
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
    )
}
