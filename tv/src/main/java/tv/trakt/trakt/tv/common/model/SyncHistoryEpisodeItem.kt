package tv.trakt.trakt.tv.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.tv.core.episodes.model.Episode
import tv.trakt.trakt.tv.core.shows.model.Show
import java.time.ZonedDateTime

@Immutable
internal data class SyncHistoryEpisodeItem(
    val id: Long,
    val watchedAt: ZonedDateTime,
    val episode: Episode,
    val show: Show,
)
