package tv.trakt.trakt.common.model.sorting

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class SortType(
    @param:StringRes val displayStringRes: Int,
    @param:DrawableRes val displayIconRes: Int?,
    val value: String,
) {
    Default(R.string.button_text_sort_default, null, "rank"),
    Added(R.string.button_text_sort_added_date, R.drawable.ic_calendar_check, "added"),
    Runtime(R.string.button_text_sort_runtime, R.drawable.ic_clock, "runtime"),
    Rating(R.string.button_text_sort_rating, R.drawable.ic_star, "percentage"),
    UserRating(R.string.button_text_sort_my_rating, R.drawable.ic_star, "my_rating"),
    Released(R.string.button_text_sort_release_date, R.drawable.ic_calendar, "released"),
    Title(R.string.button_text_sort_title, R.drawable.ic_az, "title"),
}
