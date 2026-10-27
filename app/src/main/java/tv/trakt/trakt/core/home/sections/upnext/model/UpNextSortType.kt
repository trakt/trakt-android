package tv.trakt.trakt.core.home.sections.upnext.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.common.model.sorting.SortOption
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortOrder.Asc
import tv.trakt.trakt.common.model.sorting.SortOrder.Desc
import tv.trakt.trakt.resources.R

internal enum class UpNextSortType(
    @param:StringRes override val displayStringRes: Int,
    @param:DrawableRes override val displayIconRes: Int?,
    override val value: String,
    val apiValue: String?,
) : SortOption {
    Default(R.string.button_text_sort_default, R.drawable.ic_reorder, "default", null),
    Smart(R.string.button_text_sort_smart, R.drawable.ic_smart, "smart", "smart"),
    Released(R.string.button_text_sort_release_date, R.drawable.ic_calendar, "released", "aired"),
    Remaining(R.string.button_text_sort_remaining, R.drawable.ic_hourglass, "remaining", "remaining"),
    ;

    override fun descriptionRes(order: SortOrder): Int {
        return when (this) {
            Default -> R.string.button_description_sort_default
            Smart -> R.string.button_description_sort_smart
            Released -> when (order) {
                Asc -> R.string.button_description_sort_release_date_asc
                Desc -> R.string.button_description_sort_release_date_desc
            }
            Remaining -> when (order) {
                Asc -> R.string.button_description_sort_remaining_asc
                Desc -> R.string.button_description_sort_remaining_desc
            }
        }
    }
}
