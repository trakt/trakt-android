package tv.trakt.trakt.common.helpers.coil

import coil3.Extras
import coil3.ImageLoader
import coil3.getExtra
import coil3.intercept.Interceptor
import coil3.request.ImageRequest
import coil3.request.ImageResult
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val TimeoutKey = Extras.Key(default = 15.seconds)

fun ImageLoader.Builder.timeout(timeout: Duration) =
    apply {
        extras[TimeoutKey] = timeout
    }

val ImageRequest.timeout: Duration
    get() = getExtra(TimeoutKey)

internal class TimeoutInterceptor : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val timeout = chain.request.timeout
        return if (timeout.isFinite()) {
            withTimeout(timeout) { chain.proceed() }
        } else {
            chain.proceed()
        }
    }
}
