package tv.trakt.app.tv.core.sync.model

import androidx.compose.runtime.Immutable
import tv.trakt.app.tv.common.model.TraktId
import java.time.ZonedDateTime

@Immutable
internal data class WatchedEpisode(
    val episodeId: TraktId,
    val lastWatchedAt: ZonedDateTime,
)
