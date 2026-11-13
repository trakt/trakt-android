package tv.trakt.trakt.common.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.data.dto.ImageManifestDto
import kotlin.time.Duration.Companion.seconds

private val manifestTimeout = 5.seconds

private val manifestJson = Json {
    ignoreUnknownKeys = true
}

class MediaImagesRemoteSource(
    httpClientEngine: HttpClientEngine,
    cacheStorage: CacheStorage,
) {
    // No retries: a poster waits for its manifest, so a slow lookup must fall back to the
    // plain poster instead of holding the image request.
    private val httpClient = HttpClient(httpClientEngine) {
        expectSuccess = true

        install(UserAgent) {
            agent = Config.apiUserAgent()
        }

        install(HttpCache) {
            publicStorage(cacheStorage)
        }

        install(HttpTimeout) {
            val timeoutMillis = manifestTimeout.inWholeMilliseconds
            requestTimeoutMillis = timeoutMillis
            socketTimeoutMillis = timeoutMillis
            connectTimeoutMillis = timeoutMillis
        }
    }

    suspend fun getImageManifest(
        type: String,
        id: String,
    ): ImageManifestDto {
        val response = httpClient.get("${Config.MEDIA_BASE_URL}$type/$id/images.json")
        return manifestJson.decodeFromString(response.bodyAsText())
    }
}
