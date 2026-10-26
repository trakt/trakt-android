package tv.trakt.trakt.core.profile.sections.progress.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.common.model.sorting.SortOption
import tv.trakt.trakt.resources.R

internal enum class ProgressSortType(
    @param:StringRes override val displayStringRes: Int,
    @param:DrawableRes override val displayIconRes: Int?,
    override val value: String,
    val apiValue: String?,
) : SortOption {
    Default(R.string.button_text_sort_default, R.drawable.ic_reorder, "default", null),
    Runtime(R.string.button_text_sort_runtime, R.drawable.ic_clock, "runtime", "runtime"),
    Rating(R.string.button_text_sort_rating, R.drawable.ic_star, "percentage", "rating"),
    UserRating(R.string.button_text_sort_my_rating, R.drawable.ic_star, "my_rating", "my_rating"),
    Released(R.string.button_text_sort_release_date, R.drawable.ic_calendar, "released", "released"),
    Title(R.string.button_text_sort_title, R.drawable.ic_az, "title", "title"),
}
