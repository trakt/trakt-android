package tv.trakt.trakt.common.helpers.extensions

import java.net.URLEncoder

private const val SHARE_PARAM = "share"

/**
 * Stamps the sharer's code on a shared link, falling back to `share=true` without a code.
 */
fun String.toShareUrl(shareCode: String?): String {
    if (isBlank()) return this

    val value = URLEncoder.encode(
        shareCode?.ifBlank { null } ?: "true",
        Charsets.UTF_8.name(),
    )
    val separator = if (contains('?')) "&" else "?"

    return "$this$separator$SHARE_PARAM=$value"
}
