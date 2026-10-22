package tv.trakt.trakt.common.model.sorting

import kotlinx.serialization.Serializable

@Serializable
data class Sorting(
    val type: SortType,
    val order: SortOrder,
) {
    companion object {
        val Default = Sorting(
            type = SortType.Rank,
            order = SortOrder.Asc,
        )

        val RecentlyAdded = Sorting(
            type = SortType.Added,
            order = SortOrder.Desc,
        )

        fun fromValues(
            sortBy: String?,
            sortHow: String?,
        ): Sorting? {
            val type = SortType.entries.find { it.value == sortBy } ?: return null
            val order = SortOrder.entries.find { it.value == sortHow } ?: return null
            return Sorting(type = type, order = order)
        }
    }
}
