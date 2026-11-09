package tv.trakt.trakt.common.helpers.coil

import coil3.intercept.Interceptor
import coil3.request.ImageResult
import coil3.request.SuccessResult

// Media URLs are built client-side and 404 when an image is missing, so fallbacks are resolved
// at load time. Candidates travel in the URL fragment, which is never sent to the server.
private const val FALLBACK_MARKER = "#fallback="

fun String?.withFallback(fallback: String?): String? {
    if (this == null) return fallback
    if (fallback == null) return this

    return "$this$FALLBACK_MARKER$fallback"
}

internal class FallbackInterceptor : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val data = chain.request.data as? String
        if (data == null || !data.contains(FALLBACK_MARKER)) {
            return chain.proceed()
        }

        val candidates = data.split(FALLBACK_MARKER)
        var result: ImageResult? = null
        for (url in candidates) {
            val request = chain.request.newBuilder().data(url).build()
            result = chain.withRequest(request).proceed()
            if (result is SuccessResult) return result
        }

        return checkNotNull(result)
    }
}
