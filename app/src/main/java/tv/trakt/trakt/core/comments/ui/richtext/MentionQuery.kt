package tv.trakt.trakt.core.comments.ui.richtext

import androidx.compose.runtime.Immutable

@Immutable
internal data class MentionQuery(
    val range: IntRange,
    val query: String,
)
