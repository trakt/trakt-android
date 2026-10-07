package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable

@Immutable
data class CommentGif(
    val slug: String?,
    val url: String,
)
