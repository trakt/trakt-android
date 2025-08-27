package tv.trakt.trakt.core.home.sections.activity.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class HomeActivityFilter(
    @param:StringRes val displayRes: Int,
) {
    SOCIAL(R.string.button_text_social),
    PERSONAL(R.string.button_text_personal),
}
