package tv.trakt.trakt.common.model.globalfilter

import tv.trakt.trakt.common.model.lists.ParentalGuideCategory

fun GlobalFilter.toApiParams(): Map<String, String> {
    return listOf(
        "genres" to genresParam,
        "subgenres" to subgenre?.joinToList { it },
        "years" to years?.let { "${it.first}-${it.second}" },
        "ratings" to rating?.let { "${it.first}-${it.second}" },
        "runtimes" to runtime?.let { "${it.first}-${it.second}" },
        "certifications" to certificationsParam,
        "watchnow" to availability?.joinToList { it.slug },
        "countries" to countriesParam,
        "statuses" to statusesParam,
        "ignore_watched" to hideWatched.takeIf { it }?.toString(),
        "ignore_watchlisted" to hideWatchlist.takeIf { it }?.toString(),
    ).plus(
        ParentalGuideCategory.entries.map { it.apiKey to parentalParam(it) },
    ).mapNotNull { (key, value) -> value?.let { key to it } }
        .toMap()
}

private fun <T> List<T>.joinToList(transform: (T) -> String): String? {
    return takeIf { it.isNotEmpty() }?.joinToString(",", transform = transform)
}
