package tv.trakt.trakt.app.core.sync.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.TraktId
import java.time.ZonedDateTime

@Immutable
internal data class WatchedShow(
    val showId: TraktId,
    val episodesPlays: Int,
    val episodesAired: Int,
    val lastWatchedAt: ZonedDateTime,
) {
//    val plays: Int
//        get() = seasons
//            .flatMap { it.episodes }
//            .sumOf { it.plays }
//
//    val playsDistinct: Int
//        get() = seasons
//            .flatMap { it.episodes }
//            .sumOf { it.playsDistinct }
//
//    val lastWatchedAt: Instant
//        get() = seasons
//            .flatMap { it.episodes }
//            .maxBy { it.lastWatchedAt }
//            .lastWatchedAt

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
