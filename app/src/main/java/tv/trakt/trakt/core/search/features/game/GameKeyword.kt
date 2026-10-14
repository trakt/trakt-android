package tv.trakt.trakt.core.search.features.game

private const val GAME_KEYWORD = "game"

private val NonWordRegex = Regex("[^\\p{L}\\p{N}\\s]")
private val WhitespaceRegex = Regex("\\s+")

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
