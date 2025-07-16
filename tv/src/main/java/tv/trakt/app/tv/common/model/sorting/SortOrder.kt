package tv.trakt.app.tv.common.model.sorting

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
}
