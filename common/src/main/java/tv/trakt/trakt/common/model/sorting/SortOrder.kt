package tv.trakt.trakt.common.model.sorting

enum class SortOrder(
    val value: String,
) {
    ASCENDING("asc"),
    DESCENDING("desc"),
    ;

    fun toggle(): SortOrder {
        return when (this) {
            ASCENDING -> DESCENDING
            DESCENDING -> ASCENDING
        }
    }
}
