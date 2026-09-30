package tv.trakt.trakt.core.applinks.model

private val IMDB_LINK = Regex(
    "^https?://(?:www\\.|m\\.)?imdb\\.com/(?:[a-z]{2}/)?(?:title/(tt\\d+)|name/(nm\\d+))(?:[/?#].*)?$",
    RegexOption.IGNORE_CASE,
)

internal fun parseImdbId(url: String?): String? {
    val match = IMDB_LINK.matchEntire(url ?: return null) ?: return null
    val (titleId, personId) = match.destructured
    return titleId.ifEmpty { personId }.lowercase()
}
