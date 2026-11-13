package tv.trakt.trakt.common.helpers.coil

import androidx.appcompat.app.AppCompatDelegate
import coil3.intercept.Interceptor
import coil3.request.ImageResult
import coil3.request.SuccessResult
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.serialization.SerializationException
import tv.trakt.trakt.common.data.remote.MediaImagesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

private val posterPattern =
    Regex("""^(https://media\.trakt\.tv/(movies|shows)/(\d+)/[a-z]+)/poster\.webp$""")

internal data class LocalizedPoster(
    val url: String,
    val type: String,
    val id: String,
    val language: String,
)

internal fun toPosterLanguage(locale: Locale): String? {
    val language = locale.toLanguageTag().substringBefore('-').lowercase()
    if (language.isBlank() || language == "en" || language == "und") return null

    return language
}

internal fun toLocalizedPoster(
    url: String,
    language: String,
): LocalizedPoster? {
    val match = posterPattern.matchEntire(url) ?: return null
    val (base, type, id) = match.destructured

    return LocalizedPoster(
        url = "$base/poster-$language.webp",
        type = type,
        id = id,
        language = language,
    )
}

private fun appPosterLanguage(): String? {
    val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
    return toPosterLanguage(locale)
}

internal class PosterLanguageInterceptor(
    private val remoteSource: MediaImagesRemoteSource,
) : Interceptor {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val manifests = ConcurrentHashMap<String, Deferred<Set<String>>>()

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val url = chain.request.data as? String ?: return chain.proceed()
        val language = appPosterLanguage() ?: return chain.proceed()
        val poster = toLocalizedPoster(url, language) ?: return chain.proceed()

        if (!hasPosterLanguage(poster)) {
            return chain.proceed()
        }

        val request = chain.request.newBuilder().data(poster.url).build()
        val result = chain.withRequest(request).proceed()
        if (result is SuccessResult) return result

        return chain.proceed()
    }

    private suspend fun hasPosterLanguage(poster: LocalizedPoster): Boolean {
        val key = "${poster.type}/${poster.id}"
        val manifest = manifests.computeIfAbsent(key) {
            scope.async { fetchPosterLanguages(poster.type, poster.id) }
        }

        return try {
            manifest.await().contains(poster.language)
        } catch (error: Exception) {
            error.rethrowCancellation {
                manifests.remove(key, manifest)
            }
            false
        }
    }

    // A missing or unreadable manifest means "no localized posters" and stays cached. Network
    // failures throw, so the lookup is retried on the next load.
    private suspend fun fetchPosterLanguages(
        type: String,
        id: String,
    ): Set<String> {
        return try {
            remoteSource
                .getImageManifest(type = type, id = id)
                .poster
                ?.langs
                .orEmpty()
                .toSet()
        } catch (_: ClientRequestException) {
            emptySet()
        } catch (_: SerializationException) {
            emptySet()
        }
    }
}
