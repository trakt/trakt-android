package tv.trakt.trakt.core.inreview

import android.net.Uri
import androidx.core.net.toUri

private const val WEB_SCHEME = "https"
private const val WEB_HOST = "app.trakt.tv"

private const val PARAM_STANDALONE_MODE = "standalone_mode"
private const val PARAM_SLURM = "slurm"

private val inReviewSections = setOf("year", "yir", "mir")

internal fun isTraktWebUrl(uri: Uri): Boolean {
    return uri.scheme == WEB_SCHEME && uri.host == WEB_HOST
}

internal fun isInReviewUrl(url: String): Boolean {
    val uri = url.toUri()
    if (!isTraktWebUrl(uri)) return false

    val segments = uri.pathSegments
    return segments.size >= 3 &&
        segments[0] == "users" &&
        segments[2] in inReviewSections
}

internal fun toInReviewWebUrl(
    url: String,
    slurm: String?,
): String {
    val builder = url.toUri()
        .buildUpon()
        .appendQueryParameter(PARAM_STANDALONE_MODE, "true")

    if (!slurm.isNullOrBlank()) {
        builder.appendQueryParameter(PARAM_SLURM, slurm)
    }

    return builder.build().toString()
}
