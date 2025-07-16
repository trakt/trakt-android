package tv.trakt.app.tv.core.home.sections.shows.upcoming.model

import androidx.compose.runtime.Immutable
import tv.trakt.app.tv.core.episodes.model.Episode
import tv.trakt.app.tv.core.shows.model.Show
import java.time.ZonedDateTime

@Immutable
internal data class CalendarShow(
    val show: Show,
    val episode: Episode,
    val releaseAt: ZonedDateTime,
    val isFullSeason: Boolean = false,
)
