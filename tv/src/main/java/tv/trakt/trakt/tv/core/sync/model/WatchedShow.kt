package tv.trakt.trakt.tv.core.sync.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.tv.common.model.TraktId
import java.time.ZonedDateTime

@Immutable
internal data class WatchedShow(
    val showId: TraktId,
    val episodesPlays: Int,
    val episodesAiredCount: Int,
    val lastWatchedAt: ZonedDateTime,
)
