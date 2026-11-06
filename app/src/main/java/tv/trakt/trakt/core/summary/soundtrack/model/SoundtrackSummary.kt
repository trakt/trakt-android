package tv.trakt.trakt.core.summary.soundtrack.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack

@Immutable
internal data class SoundtrackSummary(
    val total: Int,
    val playable: Int,
    val credit: String?,
) {
    companion object {
        fun from(tracks: List<SoundtrackTrack>): SoundtrackSummary {
            return SoundtrackSummary(
                total = tracks.size,
                playable = tracks.count { it.isPlayable },
                credit = tracks.dominantPerformer(),
            )
        }
    }
}

/**
 * Names whoever is behind most of the listing. A score usually has one composer,
 * a compilation has no single owner and gets no credit.
 */
private fun List<SoundtrackTrack>.dominantPerformer(): String? {
    val (performer, count) = mapNotNull { it.performer }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?: return null

    return performer.takeIf { count * 2 > size }
}

/**
 * Keeps the songs of [season] plus the series-wide ones, which carry no season.
 */
internal fun List<SoundtrackTrack>.filterBySeason(season: Int?): List<SoundtrackTrack> {
    if (season == null) return this
    return filter { it.season == null || it.season == season }
}
