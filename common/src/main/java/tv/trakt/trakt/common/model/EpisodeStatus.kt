package tv.trakt.trakt.common.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.EpisodeType.MID_SEASON_FINALE
import tv.trakt.trakt.common.model.EpisodeType.MID_SEASON_PREMIERE
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

internal val NEW_RELEASE_WINDOW = 7.days.toJavaDuration()

enum class EpisodeStatus {
    Premiere,
    Finale,
    New,
    NewPremiere,
    NewFinale,
}

fun Episode.isNew(now: Instant = nowUtcInstant()): Boolean {
    val released = releasedAt ?: return false
    if (released.isAfter(now)) return false
    return !released.plus(NEW_RELEASE_WINDOW).isBefore(now)
}

/**
 * Resolves the card status tag for a single episode or a same-day batch of episodes.
 * A premiere anywhere in the batch wins over a finale. Mid-season milestones only count
 * when [isLatestAired] is true.
 */
fun List<Episode>.episodeStatus(
    isLatestAired: Boolean = false,
    now: Instant = nowUtcInstant(),
): EpisodeStatus? {
    val first = firstOrNull() ?: return null
    val isNew = first.isNew(now)

    return when {
        any { it.hasPremiere(isLatestAired) } -> if (isNew) EpisodeStatus.NewPremiere else EpisodeStatus.Premiere
        any { it.hasFinale(isLatestAired) } -> if (isNew) EpisodeStatus.NewFinale else EpisodeStatus.Finale
        isNew -> EpisodeStatus.New
        else -> null
    }
}

@Composable
fun List<Episode>.rememberEpisodeStatus(isLatestAired: Boolean = false): EpisodeStatus? {
    return remember(this, isLatestAired) {
        episodeStatus(isLatestAired)
    }
}

@Composable
fun Episode.rememberEpisodeStatus(isLatestAired: Boolean = false): EpisodeStatus? {
    return remember(this, isLatestAired) {
        listOf(this).episodeStatus(isLatestAired)
    }
}

private fun Episode.hasPremiere(isLatestAired: Boolean): Boolean {
    if (type?.isPremiere == true) return true
    return type == MID_SEASON_PREMIERE && isLatestAired
}

private fun Episode.hasFinale(isLatestAired: Boolean): Boolean {
    if (type?.isFinale == true) return true
    return type == MID_SEASON_FINALE && isLatestAired
}
