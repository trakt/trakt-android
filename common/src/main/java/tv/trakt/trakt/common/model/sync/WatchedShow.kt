package tv.trakt.trakt.common.model.sync

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.TraktId
import java.time.ZonedDateTime

@Immutable
data class WatchedShow(
    val showId: TraktId,
    val episodesPlays: Int,
    val episodesPlaysWithoutSpecials: Int,
    val episodesAired: Int,
    val lastWatchedAt: ZonedDateTime,
) {
    data class Season(
        val id: TraktId,
        val number: Int,
        val episodes: ImmutableList<Episode>,
    )

    data class Episode(
        val id: TraktId,
        val plays: Int,
        val playsDistinct: Int,
        val lastWatchedAt: ZonedDateTime,
    )
}
