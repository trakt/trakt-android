package tv.trakt.trakt.tv.core.sync.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.TraktId
import java.time.ZonedDateTime

@Immutable
internal data class WatchedMovie(
    val movieId: TraktId,
    val plays: Int,
    val lastWatchedAt: ZonedDateTime,
)
