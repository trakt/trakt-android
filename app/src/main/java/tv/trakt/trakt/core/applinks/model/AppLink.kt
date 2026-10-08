package tv.trakt.trakt.core.applinks.model

import android.net.Uri
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.toSlugId

private const val APP_LINK_SCHEME = "https"
private const val APP_LINK_HOST = "app.trakt.tv"

internal sealed interface AppLink {
    data class Show(
        val slug: SlugId,
    ) : AppLink

    data class Movie(
        val slug: SlugId,
    ) : AppLink

    data class Person(
        val slug: SlugId,
    ) : AppLink

    data class Imdb(
        val imdbId: String,
    ) : AppLink
}

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

    return when (type) {
        "shows" -> AppLink.Show(slug.toSlugId())
        "movies" -> AppLink.Movie(slug.toSlugId())
        "people" -> AppLink.Person(slug.toSlugId())
        else -> null
    }
}
