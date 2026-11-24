package tv.trakt.trakt.core.lists.sections.smart.templates.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.lists.SMART_LIST_OPEN_VOTES_CEILING
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListTemplate

private const val PERCENT_CEILING = 100
private const val RUNTIME_CEILING = 500
private const val SCORE_CEILING = 10F
private const val LETTERBOXD_CEILING = 5F

internal fun visibleSmartListTemplates(
    templates: List<SmartListTemplate>,
    lists: List<SmartList>,
    mode: MediaMode,
): ImmutableList<SmartListTemplate> {
    return templates
        .filter { template ->
            SmartListTemplateCopy.fromId(template.id) != null &&
                template.fitsMode(mode) &&
                !template.isCoveredBy(lists)
        }
        .toImmutableList()
}

private fun SmartListTemplate.fitsMode(mode: MediaMode): Boolean {
    return filters.media == MediaMode.Media || mode == MediaMode.Media || filters.media == mode
}

private fun SmartListTemplate.isCoveredBy(lists: List<SmartList>): Boolean {
    val closedFilters = filters.withClosedRanges()
    return lists.any { it.filters.withClosedRanges() == closedFilters }
}

private fun SmartListFilters.withClosedRanges(): SmartListFilters {
    return copy(
        ratings = ratings.closedWith(PERCENT_CEILING),
        rtMeters = rtMeters.closedWith(PERCENT_CEILING),
        rtUserMeters = rtUserMeters.closedWith(PERCENT_CEILING),
        runtimes = runtimes.closedWith(RUNTIME_CEILING),
        votes = votes.closedWith(SMART_LIST_OPEN_VOTES_CEILING),
        imdbVotes = imdbVotes.closedWith(SMART_LIST_OPEN_VOTES_CEILING),
        imdbRatings = imdbRatings.closedWith(SCORE_CEILING),
        tmdbRatings = tmdbRatings.closedWith(SCORE_CEILING),
        malRatings = malRatings.closedWith(SCORE_CEILING),
        letterboxdRatings = letterboxdRatings.closedWith(LETTERBOXD_CEILING),
    )
}

private fun <T> ImmutableList<T>?.closedWith(ceiling: T): ImmutableList<T>? {
    if (this?.size != 1) return this
    return persistentListOf(first(), ceiling)
}
