package tv.trakt.trakt.core.home.sections.upnext.model

import tv.trakt.trakt.common.model.sorting.SortOrder

internal data class UpNextSorting(
    val type: UpNextSortType,
    val order: SortOrder,
) {
    val isDefault: Boolean
        get() = type == UpNextSortType.Default

    companion object {
        val Default = UpNextSorting(
            type = UpNextSortType.Default,
            order = SortOrder.Desc,
        )
    }
}
