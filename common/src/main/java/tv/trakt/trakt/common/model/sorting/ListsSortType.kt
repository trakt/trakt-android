package tv.trakt.trakt.common.model.sorting

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class ListsSortType(
    @param:StringRes override val displayStringRes: Int,
    @param:DrawableRes override val displayIconRes: Int?,
    override val value: String,
) : SortOption {
    Rank(R.string.button_text_sort_rank, R.drawable.ic_format_bullet_list, "rank"),
    Name(R.string.button_text_sort_name, R.drawable.ic_az, "name"),
    Updated(R.string.button_text_sort_updated_date, R.drawable.ic_calendar, "updated_at"),
    Created(R.string.button_text_sort_created_date, R.drawable.ic_calendar, "created_at"),
}
