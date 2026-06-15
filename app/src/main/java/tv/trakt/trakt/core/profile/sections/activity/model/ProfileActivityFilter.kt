package tv.trakt.trakt.core.profile.sections.activity.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class ProfileActivityFilter(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    Ratings(
        displayRes = R.string.button_text_activity_ratings,
        iconRes = R.drawable.ic_star,
    ),
    Comments(
        displayRes = R.string.button_text_activity_reviews,
        iconRes = R.drawable.ic_comment,
    ),
}
