package tv.trakt.trakt.core.comments.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class CommentMention(
    val name: String,
    val href: String,
    val detail: String? = null,
)
