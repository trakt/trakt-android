package tv.trakt.trakt.core.lists.features.smart.search

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.SearchRemoteSource
import tv.trakt.trakt.common.data.remote.SmartListFilterOptionsRemoteSource

private const val SEARCH_LIMIT = 20

internal class SearchSmartListFilterOptionsUseCase(
    private val filterOptionsRemoteSource: SmartListFilterOptionsRemoteSource,
    private val searchRemoteSource: SearchRemoteSource,
) {
    suspend fun search(
        kind: SmartListSearchKind,
        query: String,
    ): ImmutableList<SmartListSearchOption> {
        val options = when (kind) {
            SmartListSearchKind.People -> {
                searchRemoteSource
                    .getPeople(query = query, limit = SEARCH_LIMIT, extended = "min")
                    .mapNotNull { it.person }
                    .map { SmartListSearchOption(value = it.ids.slug, label = it.name) }
            }
            SmartListSearchKind.Keywords -> {
                getNamedOptions("keywords", query)
            }
            SmartListSearchKind.Networks -> {
                getNamedOptions("networks", query)
            }
            SmartListSearchKind.Studios -> {
                filterOptionsRemoteSource
                    .getFilterOptions(kind = "studios", query = query, limit = SEARCH_LIMIT)
                    .map { SmartListSearchOption(value = it.slug ?: it.name, label = it.name) }
            }
        }

        return options
            .distinctBy { it.value }
            .toImmutableList()
    }

    private suspend fun getNamedOptions(
        kind: String,
        query: String,
    ): List<SmartListSearchOption> {
        return filterOptionsRemoteSource
            .getFilterOptions(kind = kind, query = query, limit = SEARCH_LIMIT)
            .map { SmartListSearchOption(value = it.name, label = it.name) }
    }
}
