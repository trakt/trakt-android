package tv.trakt.app.tv.common.model.sorting

enum class SortType(
    val value: String,
) {
    RANK("rank"),
    ADDED("added"),
    TITLE("title"),
    RELEASED("released"),
    RUNTIME("runtime"),
    POPULARITY("popularity"),
    PERCENTAGE("percentage"),
    VOTES("votes"),
    ;

    companion object {
        fun fromString(value: String): SortType? {
            return entries.find { it.value == value.lowercase() }
        }
    }
}
