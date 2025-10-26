package tv.trakt.trakt.core.reactions.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface ReactionsUpdates {
    fun notifyUpdate(
        commentId: Int,
        source: Source,
    )

    fun observeUpdates(source: Source): Flow<Pair<Int, Instant>>

    enum class Source {
        ALL_COMMENTS,
        COMMENT_DETAILS,
    }
}
