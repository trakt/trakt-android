package tv.trakt.trakt.core.lists.sections.personal.model

import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.sorting.ListsSortType.Created
import tv.trakt.trakt.common.model.sorting.ListsSortType.Name
import tv.trakt.trakt.common.model.sorting.ListsSortType.Rank
import tv.trakt.trakt.common.model.sorting.ListsSortType.Updated
import tv.trakt.trakt.common.model.sorting.ListsSorting
import tv.trakt.trakt.common.model.sorting.SortOrder.Asc
import tv.trakt.trakt.common.model.sorting.SortOrder.Desc

/**
 * Sorts lists that are already in rank order, which is how the local storage keeps them.
 */
internal fun sortPersonalLists(
    lists: List<CustomList>,
    sorting: ListsSorting,
): List<CustomList> {
    val ascending = when (sorting.type) {
        Rank -> lists
        Name -> lists.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        Updated -> lists.sortedBy { it.updatedAt }
        Created -> lists.sortedBy { it.createdAt }
    }

    return when (sorting.order) {
        Asc -> ascending
        Desc -> ascending.reversed()
    }
}
