package tv.trakt.trakt.core.search.features.game

import tv.trakt.trakt.common.core.search.SearchState
import tv.trakt.trakt.common.core.search.model.SearchFilter

private const val GAME_KEYWORD = "game"

private val NonWordRegex = Regex("[^\\p{L}\\p{N}\\s]")
private val WhitespaceRegex = Regex("\\s+")

private val GameCardFilters = setOf(
    SearchFilter.MEDIA,
    SearchFilter.SHOWS,
    SearchFilter.MOVIES,
)

internal val SearchState.showGameCard: Boolean
    get() = input.filter in GameCardFilters && isGameKeyword(input.query) && !searchResult?.items.isNullOrEmpty()

internal fun isGameKeyword(query: String?): Boolean {
    return normalizePhrase(query) == GAME_KEYWORD
}

private fun normalizePhrase(value: String?): String {
    return value.orEmpty()
        .lowercase()
        .replace(NonWordRegex, "")
        .replace(WhitespaceRegex, " ")
        .trim()
}
