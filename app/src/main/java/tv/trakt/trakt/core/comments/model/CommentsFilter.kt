package tv.trakt.trakt.core.comments.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class CommentsFilter(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    POPULAR(R.string.text_sort_comments_popular, R.drawable.ic_popular),
    RECENT(R.string.text_sort_comments_recent, R.drawable.ic_recent),
}
