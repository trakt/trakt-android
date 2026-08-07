package tv.trakt.trakt.core.summary.people.helpers

private val NON_PREFERRED_CREDITS_FILTERS = setOf("self", "narrator")

/**
 * Picks the default department filter for a person's credits list. Honors
 * [knownForDepartment] when it actually matches one of [listItems]'s
 * departments. Otherwise, ranks the remaining departments by credit count,
 * preferring acting/crew departments over self/narrator - but falling back
 * to those when they're the only credits the person has, so a documentary
 * subject with only "self" appearances still gets a populated default
 * instead of an empty list. Returns null when [listItems] is empty - callers
 * should fall back to their own default rather than crash.
 */
fun <T> resolveSelectedCreditsFilter(
    listItems: Map<String, List<T>>,
    knownForDepartment: String?,
): String? {
    val validKnownFor = listItems.keys.firstOrNull { it.equals(knownForDepartment, ignoreCase = true) }
    if (validKnownFor != null) return validKnownFor

    val preferred = listItems.keys.filterNot { it.lowercase() in NON_PREFERRED_CREDITS_FILTERS }
    val candidates = preferred.ifEmpty { listItems.keys }

    return candidates.maxByOrNull { listItems.getValue(it).size }
}
