package tv.trakt.trakt.core.comments.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class CommentsFilter(
    @param:StringRes val displayRes: Int,
) {
    POPULAR(R.string.text_sort_comments_popular),
    RECENT(R.string.text_sort_comments_recent),
}
