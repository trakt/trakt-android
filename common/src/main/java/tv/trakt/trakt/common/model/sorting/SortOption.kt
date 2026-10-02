package tv.trakt.trakt.common.model.sorting

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface SortOption {
    val value: String

    @get:StringRes
    val displayStringRes: Int

    @get:DrawableRes
    val displayIconRes: Int?
}
