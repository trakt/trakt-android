package tv.trakt.trakt.core.home.sections.activity.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class HomeActivityFilter(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    SOCIAL(R.string.button_text_social, R.drawable.ic_social),
    PERSONAL(R.string.button_text_personal, R.drawable.ic_check_round),
}
