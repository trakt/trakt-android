package tv.trakt.trakt.core.applinks.model

import android.net.Uri
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.toSlugId

private const val APP_LINK_SCHEME = "https"
private const val APP_LINK_HOST = "app.trakt.tv"
private const val APP_LINK_SHARE_PARAM = "share"

internal sealed interface AppLink {
    val share: AppLinkShare?

    data class Show(
        val slug: SlugId,
        override val share: AppLinkShare? = null,
    ) : AppLink

    data class Movie(
        val slug: SlugId,
        override val share: AppLinkShare? = null,
    ) : AppLink

    data class Person(
        val slug: SlugId,
        override val share: AppLinkShare? = null,
    ) : AppLink

    data class Imdb(
        val imdbId: String,
    ) : AppLink {
        override val share: AppLinkShare? = null
    }
}

/**
 * Sharer's code from the `share` param of a shared link, with the link it arrived on.
 */
internal data class AppLinkShare(
    val code: String,
    val url: String,
)

internal fun parseAppLink(uri: Uri): AppLink? {
    val imdbId = parseImdbId(uri.toString())
    if (imdbId != null) {
        return AppLink.Imdb(imdbId)
    }

    if (uri.scheme != APP_LINK_SCHEME || uri.host != APP_LINK_HOST) {
        return null
    }

    val type = uri.pathSegments.getOrNull(0)
    val slug = uri.pathSegments.getOrNull(1)
    if (slug.isNullOrBlank()) {
        return null
    }

    val share = uri.getQueryParameter(APP_LINK_SHARE_PARAM)?.let {
        AppLinkShare(code = it, url = uri.toString())
    }

    return when (type) {
        "shows" -> AppLink.Show(slug.toSlugId(), share)
        "movies" -> AppLink.Movie(slug.toSlugId(), share)
        "people" -> AppLink.Person(slug.toSlugId(), share)
        else -> null
    }
}
