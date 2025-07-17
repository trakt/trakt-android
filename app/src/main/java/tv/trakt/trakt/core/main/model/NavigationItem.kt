package tv.trakt.trakt.core.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
internal data class NavigationItem(
    val destination: Any,
    @param:StringRes val label: Int,
    @param:DrawableRes val iconOn: Int,
    @param:DrawableRes val iconOff: Int,
)
