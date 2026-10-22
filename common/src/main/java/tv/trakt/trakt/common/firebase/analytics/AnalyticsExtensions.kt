package tv.trakt.trakt.common.firebase.analytics

import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting

private const val SORT_BY_DEFAULT = "default"

fun Analytics.Lists.logListSort(sorting: Sorting) {
    logListSort(
        sortBy = if (sorting.type == SortType.Rank) SORT_BY_DEFAULT else sorting.type.value,
        sortHow = sorting.order.value,
    )
}
