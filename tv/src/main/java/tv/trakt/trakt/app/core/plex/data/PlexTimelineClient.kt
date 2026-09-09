package tv.trakt.trakt.app.core.plex.data

import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.isSuccess
import timber.log.Timber
import tv.trakt.trakt.app.BuildConfig
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation

/**
 * Tells the Plex Media Server where playback is (`POST /:/timeline`), the same call other Plex
 * clients make, so the item goes in-progress/watched and can be resumed elsewhere.
 * Best effort: failures are logged and never reach the player.
 */
internal class PlexTimelineClient(
    httpClientEngine: HttpClientEngine,
    private val clientIdentifier: String,
) {
    enum class State {
        Playing,
        Paused,
        Stopped,
    }

    /** One playback snapshot. [baseUrl] is the server base of whichever stream URL is currently playing. */
    data class Snapshot(
        val baseUrl: String,
        val position: Long,
        val duration: Long,
        val state: State,
    )

    // Bare client on purpose: the authorised Trakt config would send Trakt auth headers to the Plex server.
    private val httpClient = HttpClient(httpClientEngine) {
        // A redirect would forward X-Plex-Token to whatever host the server names.
        followRedirects = false
        install(HttpTimeout) {
            requestTimeoutMillis = 5_000
        }
    }

    suspend fun report(
        ratingKey: String?,
        token: String?,
        snapshot: Snapshot,
    ) {
        if (ratingKey.isNullOrBlank() || token.isNullOrBlank()) return
        if (snapshot.duration <= 0) return

        try {
            val response = httpClient.post("${snapshot.baseUrl}/:/timeline") {
                parameter("ratingKey", ratingKey)
                parameter("key", "/library/metadata/$ratingKey")
                parameter("time", snapshot.position)
                parameter("state", snapshot.state.name.lowercase())
                parameter("duration", snapshot.duration)
                header("X-Plex-Token", token)
                header("X-Plex-Client-Identifier", "trakt-android-tv-$clientIdentifier")
                header("X-Plex-Product", "Trakt")
                header("X-Plex-Version", BuildConfig.VERSION_NAME)
                header("X-Plex-Platform", "Android")
                header("X-Plex-Device", Build.MODEL)
                header("X-Plex-Device-Name", "Trakt Android TV")
            }
            if (!response.status.isSuccess()) {
                Timber.w("Plex timeline failed: HTTP %d", response.status.value)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "Plex timeline failed")
            }
        }
    }
}
