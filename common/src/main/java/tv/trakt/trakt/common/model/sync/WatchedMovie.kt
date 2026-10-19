package tv.trakt.trakt.common.model.sync

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.TraktId
import java.time.ZonedDateTime

@Immutable
data class WatchedMovie(
    val movieId: TraktId,
    val plays: Int,
    val lastWatchedAt: ZonedDateTime,
)
