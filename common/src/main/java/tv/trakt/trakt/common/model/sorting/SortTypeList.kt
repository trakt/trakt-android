package tv.trakt.trakt.common.model.sorting

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class SortTypeList(
    @param:StringRes val displayStringRes: Int,
) {
    DEFAULT(R.string.text_sort_default),
    ADDED(R.string.text_sort_date_added),
    RUNTIME(R.string.text_sort_runtime),
    RATING(R.string.text_sort_rating),
    RELEASED(R.string.text_sort_release_date),
}
