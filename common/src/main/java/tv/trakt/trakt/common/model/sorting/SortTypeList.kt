package tv.trakt.trakt.common.model.sorting

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class SortTypeList(
    @param:StringRes val displayStringRes: Int,
    val value: String,
) {
    DEFAULT(R.string.button_text_sort_default, "rank"),
    ADDED(R.string.button_text_sort_added_date, "added"),
    RUNTIME(R.string.button_text_sort_runtime, "runtime"),
    RATING(R.string.button_text_sort_rating, "percentage"),
    RELEASED(R.string.button_text_sort_release_date, "released"),
}
