package tv.trakt.trakt.widgets.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

internal enum class WidgetBackground(
    @StringRes val displayStringRes: Int,
) {
    Solid(R.string.text_settings_background_solid),
    SemiTransparent(R.string.text_settings_background_translucent),
    None(R.string.text_settings_background_none),
}
