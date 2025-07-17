package tv.trakt.trakt.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
internal data class NavigationItem(
    @param:StringRes val label: Int,
    @param:DrawableRes val iconOn: Int,
    @param:DrawableRes val iconOff: Int,
)
