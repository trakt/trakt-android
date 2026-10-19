package tv.trakt.trakt.common.model.sync

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.TraktId
import java.time.ZonedDateTime

@Immutable
data class WatchedEpisode(
    val episodeId: TraktId,
    val lastWatchedAt: ZonedDateTime,
)
