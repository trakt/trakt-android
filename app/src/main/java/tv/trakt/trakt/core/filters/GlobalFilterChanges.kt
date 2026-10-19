package tv.trakt.trakt.core.filters

import tv.trakt.trakt.common.model.globalfilter.GlobalFilter

internal data class GlobalFilterChange(
    val key: String,
    val set: Boolean,
)

private val filterKeys: List<Pair<String, (GlobalFilter) -> Any?>> = listOf(
    "genres" to GlobalFilter::genre,
    "years" to GlobalFilter::years,
    "runtimes" to GlobalFilter::runtime,
    "watchnow" to GlobalFilter::availability,
    "certifications" to GlobalFilter::certification,
    "countries" to GlobalFilter::region,
    "countries" to GlobalFilter::countries,
    "statuses" to GlobalFilter::statuses,
    "ratings" to GlobalFilter::rating,
    "ignore_watched" to GlobalFilter::hideWatched,
    "ignore_watchlisted" to GlobalFilter::hideWatchlist,
)

internal fun GlobalFilter.changesFrom(previous: GlobalFilter): List<GlobalFilterChange> {
    return filterKeys
        .filter { (_, selector) -> selector(this) != selector(previous) }
        .map { (key, selector) -> GlobalFilterChange(key = key, set = selector(this).hasValue()) }
        .distinctBy { it.key }
}

private fun Any?.hasValue(): Boolean {
    return when (this) {
        null -> false
        is Boolean -> this
        is Collection<*> -> isNotEmpty()
        else -> true
    }
}
