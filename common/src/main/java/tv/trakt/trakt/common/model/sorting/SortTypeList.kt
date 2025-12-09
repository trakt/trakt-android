package tv.trakt.trakt.common.model.sorting

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class SortTypeList(
    @param:StringRes val displayStringRes: Int,
    val value: String,
) {
    DEFAULT(R.string.text_sort_default, "rank"),
    ADDED(R.string.text_sort_date_added, "added"),
    RUNTIME(R.string.text_sort_runtime, "runtime"),
    RATING(R.string.text_sort_rating, "percentage"),
    RELEASED(R.string.text_sort_release_date, "released"),
}
