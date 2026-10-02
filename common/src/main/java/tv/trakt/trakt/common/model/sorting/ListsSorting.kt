package tv.trakt.trakt.common.model.sorting

data class ListsSorting(
    val type: ListsSortType,
    val order: SortOrder,
) {
    companion object {
        val Default = ListsSorting(
            type = ListsSortType.Rank,
            order = SortOrder.Asc,
        )
    }
}
