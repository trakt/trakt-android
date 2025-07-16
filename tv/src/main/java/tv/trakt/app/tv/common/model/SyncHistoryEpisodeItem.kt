package tv.trakt.app.tv.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.app.tv.core.episodes.model.Episode
import tv.trakt.app.tv.core.shows.model.Show
import java.time.ZonedDateTime

@Immutable
internal data class SyncHistoryEpisodeItem(
    val id: Long,
    val watchedAt: ZonedDateTime,
    val episode: Episode,
    val show: Show,
)
