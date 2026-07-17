package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.helpers

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.OtherDateBound
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.Now
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.OtherDate
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.ReleaseDate
import java.time.Instant

// Timeline anchors per action:
//  - Now: last (selected) episode = now, earlier ones step backwards by their runtime.
//  - Release date: each episode's own release date.
//  - Other + Start: first episode = anchor, progresses forward by each runtime.
//  - Other + End: last episode = anchor, steps backwards by each runtime.
internal fun computeWatchedTimestamps(
    episodes: ImmutableList<Episode>,
    action: WatchedUntilAction,
    otherBound: OtherDateBound?,
    otherAnchor: Instant?,
): List<Instant?> =
    when (action) {
        Now -> backwardFrom(episodes, nowUtcInstant())
        ReleaseDate -> episodes.map { it.releasedAt }
        OtherDate -> when {
            otherAnchor == null -> List(episodes.size) { null }
            otherBound == OtherDateBound.Start -> forwardFrom(episodes, otherAnchor)
            otherBound == OtherDateBound.End -> backwardFrom(episodes, otherAnchor)
            else -> List(episodes.size) { null }
        }
    }

private fun forwardFrom(
    episodes: ImmutableList<Episode>,
    anchor: Instant,
): List<Instant?> {
    var accumulated = anchor
    val result = arrayOfNulls<Instant>(episodes.size)
    for (index in episodes.indices) {
        result[index] = accumulated
        accumulated = accumulated.plusMillis(episodes[index].runtime?.inWholeMilliseconds ?: 0)
    }
    return result.toList()
}

private fun backwardFrom(
    episodes: ImmutableList<Episode>,
    anchor: Instant,
): List<Instant?> {
    var accumulated = anchor
    val result = arrayOfNulls<Instant>(episodes.size)
    for (index in episodes.indices.reversed()) {
        result[index] = accumulated
        accumulated = accumulated.minusMillis(episodes[index].runtime?.inWholeMilliseconds ?: 0)
    }
    return result.toList()
}
