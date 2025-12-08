package tv.trakt.trakt.common.model.sorting

enum class SortOrder(
    val value: String,
) {
    ASCENDING("asc"),
    DESCENDING("desc"),
    ;

    companion object {
        fun fromString(value: String): SortOrder? {
            return entries.find { it.value == value.lowercase() }
        }
    }

    fun toggle(): SortOrder {
        return when (this) {
            ASCENDING -> DESCENDING
            DESCENDING -> ASCENDING
        }
    }
}
