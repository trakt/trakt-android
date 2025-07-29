package tv.trakt.trakt.tv.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.tv.core.episodes.model.Episode
import java.time.ZonedDateTime

@Immutable
internal data class SyncHistoryEpisodeItem(
    val id: Long,
    val watchedAt: ZonedDateTime,
    val episode: Episode,
    val show: Show,
)
