package tv.trakt.trakt.core.comments.ui.richtext

import tv.trakt.trakt.core.comments.model.CommentMention

internal fun List<CommentMention>.toMentionMatches(
    query: String,
    limit: Int,
): List<CommentMention> {
    val normalizedQuery = query.trim().lowercase()

    return filter { it.name.lowercase().contains(normalizedQuery) }
        .take(limit)
}
