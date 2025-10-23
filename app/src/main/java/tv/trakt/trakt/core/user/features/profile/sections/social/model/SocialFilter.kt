package tv.trakt.trakt.core.user.features.profile.sections.social.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class SocialFilter(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    FOLLOWING(
        displayRes = R.string.text_following,
        iconRes = R.drawable.ic_following,
    ),
    FOLLOWERS(
        displayRes = R.string.text_followers,
        iconRes = R.drawable.ic_followers,
    ),
}
