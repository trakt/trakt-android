package tv.trakt.trakt.core.profile.sections.progress.model

import tv.trakt.trakt.common.model.sorting.SortOrder

internal data class ProgressSorting(
    val type: ProgressSortType,
    val order: SortOrder,
) {
    val isDefault: Boolean
        get() = type == ProgressSortType.Default

    companion object {
        val Default = ProgressSorting(
            type = ProgressSortType.Default,
            order = SortOrder.Desc,
        )
    }
}
