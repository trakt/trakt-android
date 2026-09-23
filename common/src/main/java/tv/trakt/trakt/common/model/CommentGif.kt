package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable

@Immutable
data class CommentGif(
    val url: String,
    val size: Pair<Int, Int>,
)
