package tv.trakt.trakt.core.comments.helpers

import tv.trakt.trakt.common.firebase.analytics.Analytics.CommentContent
import tv.trakt.trakt.common.model.CommentGif

internal fun commentAnalyticsContent(
    text: String,
    gif: CommentGif?,
): CommentContent {
    return when {
        gif == null -> CommentContent.Text
        text.isBlank() -> CommentContent.Gif
        else -> CommentContent.TextAndGif
    }
}
