package tv.trakt.trakt.core.discover.sections.releases.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R

internal enum class ReleaseType(
    @param:StringRes val textRes: Int,
    @param:DrawableRes val iconRes: Int,
    val iconSize: Dp,
) {
    All(
        textRes = R.string.option_text_all,
        iconRes = R.drawable.ic_shows_movies,
        iconSize = 16.dp,
    ),
    Premiere(
        textRes = R.string.tag_text_premiere,
        iconRes = R.drawable.ic_discover_off,
        iconSize = 18.dp,
    ),
    Finale(
        textRes = R.string.tag_text_finale,
        iconRes = R.drawable.ic_flag,
        iconSize = 19.dp,
    ),
}
