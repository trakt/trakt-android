package tv.trakt.trakt.common.model.sorting

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class SortOrder(
    val value: String,
    @param:StringRes val displayStringRes: Int,
    @param:DrawableRes val displayIconRes: Int,
) {
    Asc("asc", R.string.button_text_sort_asc, R.drawable.ic_sort_asc),
    Desc("desc", R.string.button_text_sort_desc, R.drawable.ic_sort_desc),
    ;

    fun toggle(): SortOrder {
        return when (this) {
            Asc -> Desc
            Desc -> Asc
        }
    }
}
