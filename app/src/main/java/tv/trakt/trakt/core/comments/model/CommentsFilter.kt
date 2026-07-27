package tv.trakt.trakt.core.comments.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

@Keep
enum class CommentsFilter(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    Popular(R.string.text_sort_comments_popular, R.drawable.ic_popular),
    Recent(R.string.text_sort_comments_recent, R.drawable.ic_recent),
}
