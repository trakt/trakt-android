package tv.trakt.trakt.app.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.common.model.Show
import java.time.ZonedDateTime

@Immutable
internal data class SyncHistoryEpisodeItem(
    val id: Long,
    val watchedAt: ZonedDateTime,
    val episode: Episode,
    val show: Show,
)
