package tv.trakt.trakt.common.helpers.extensions

import tv.trakt.trakt.common.model.MediaType

/**
 * Search term the GIF picker uses while the input is empty, e.g. "Ted Lasso show". Season and
 * episode comments belong to a show, so they share the show suffix. Null falls back to trending.
 */
fun MediaType.toGifQuery(title: String?): String? {
    if (title.isNullOrBlank()) return null

    val suffix = when (this) {
        MediaType.Movie -> "movie"
        MediaType.Show,
        MediaType.Season,
        MediaType.Episode,
        -> "show"
    }
    return "${title.trim()} $suffix"
}
