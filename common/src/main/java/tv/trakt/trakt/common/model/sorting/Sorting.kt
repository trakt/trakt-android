package tv.trakt.trakt.common.model.sorting

data class Sorting(
    val type: SortTypeList,
    val order: SortOrder,
) {
    companion object {
        val Default = Sorting(
            type = SortTypeList.DEFAULT,
            order = SortOrder.ASCENDING,
        )

        val RecentlyAdded = Sorting(
            type = SortTypeList.ADDED,
            order = SortOrder.DESCENDING,
        )
    }
}
