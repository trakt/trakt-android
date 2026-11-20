package tv.trakt.trakt.common.helpers.coil

import coil3.intercept.Interceptor
import coil3.request.ImageResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.data.local.PortraitTokenLocalSource
import tv.trakt.trakt.common.data.remote.PortraitsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.PortraitToken
import java.time.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val PORTRAITS_PREFIX = "${Config.MEDIA_BASE_URL}portraits/"
private const val VERIFY_PARAM = "verify"

// Tokens stay valid until expires_at, so one is reused until shortly before that.
private val EXPIRY_MARGIN = 12.hours.toJavaDuration()

// Kept well below the image request timeout so the image itself still has time to load.
private val FETCH_TIMEOUT = 5.seconds
private val FAILURE_BACKOFF = 1.minutes.toJavaDuration()

internal fun withPortraitToken(
    url: String,
    token: String,
): String? {
    if (!url.startsWith(PORTRAITS_PREFIX)) return null
    if (url.contains('?')) return null

    return "$url?$VERIFY_PARAM=$token"
}

internal class PortraitTokenInterceptor(
    private val remoteSource: PortraitsRemoteSource,
    private val localSource: PortraitTokenLocalSource,
) : Interceptor {
    private val mutex = Mutex()
    private var cached: PortraitToken? = null
    private var retryAfter: Instant = Instant.MIN

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val url = chain.request.data as? String ?: return chain.proceed()
        if (!url.startsWith(PORTRAITS_PREFIX)) return chain.proceed()

        val token = getToken() ?: return chain.proceed()
        val signedUrl = withPortraitToken(url, token) ?: return chain.proceed()

        // The token rotates weekly, so cache entries stay keyed by the plain URL.
        val request = chain.request.newBuilder()
            .data(signedUrl)
            .memoryCacheKey(chain.request.memoryCacheKey ?: url)
            .diskCacheKey(chain.request.diskCacheKey ?: url)
            .build()

        return chain.withRequest(request).proceed()
    }

    private suspend fun getToken(): String? {
        return mutex.withLock {
            val now = nowUtcInstant()
            cached?.takeIf { it.isValid(now) }?.let { return@withLock it.token }

            try {
                localSource.getToken()
                    ?.takeIf { it.isValid(now) }
                    ?.let { stored ->
                        cached = stored
                        return@withLock stored.token
                    }

                if (now.isBefore(retryAfter)) return@withLock null

                val dto = withTimeoutOrNull(FETCH_TIMEOUT) { remoteSource.getToken() }
                if (dto == null) {
                    retryAfter = now.plus(FAILURE_BACKOFF)
                    return@withLock null
                }

                PortraitToken(
                    token = dto.token,
                    expiresAt = dto.expiresAt.toInstant(),
                )
                    .also {
                        cached = it
                        localSource.setToken(it)
                    }
                    .token
            } catch (error: Exception) {
                error.rethrowCancellation()
                retryAfter = now.plus(FAILURE_BACKOFF)
                null
            }
        }
    }

    private fun PortraitToken.isValid(now: Instant): Boolean {
        return now.isBefore(expiresAt.minus(EXPIRY_MARGIN))
    }
}
