package tv.trakt.trakt.core.main.model

private val IMDB_LINK = Regex(
    "^https?://(?:www\\.|m\\.)?imdb\\.com/(?:[a-z]{2}/)?(?:title/(tt\\d+)|name/(nm\\d+))(?:[/?#].*)?$",
    RegexOption.IGNORE_CASE,
)

internal sealed interface ImdbLink {
    val imdbId: String

    data class Title(
        override val imdbId: String,
    ) : ImdbLink

    data class Person(
        override val imdbId: String,
    ) : ImdbLink

    companion object {
        fun parse(url: String?): ImdbLink? {
            val match = IMDB_LINK.matchEntire(url ?: return null) ?: return null
            val (titleId, personId) = match.destructured
            return when {
                titleId.isNotEmpty() -> Title(titleId.lowercase())
                else -> Person(personId.lowercase())
            }
        }
    }
}
