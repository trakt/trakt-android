package tv.trakt.trakt.core.comments.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface CommentsUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(source: Source): Flow<Instant>

    enum class Source {
        ALL_COMMENTS,
        COMMENT_DETAILS,
    }
}
