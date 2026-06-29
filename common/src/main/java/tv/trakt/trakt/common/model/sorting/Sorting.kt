package tv.trakt.trakt.common.model.sorting

data class Sorting(
    val type: SortType,
    val order: SortOrder,
) {
    companion object {
        val Default = Sorting(
            type = SortType.Default,
            order = SortOrder.Asc,
        )

        val RecentlyAdded = Sorting(
            type = SortType.Added,
            order = SortOrder.Desc,
        )
    }
}
