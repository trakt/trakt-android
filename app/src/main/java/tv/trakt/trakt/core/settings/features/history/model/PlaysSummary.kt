package tv.trakt.trakt.core.settings.features.history.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Immutable
internal data class PlaysSummary(
    val unique: Int = 0,
    val total: Int = 0,
    val duplicateIds: ImmutableList<Long> = persistentListOf(),
) {
    val duplicates: Int
        get() = duplicateIds.size

    companion object {
        fun fromPlays(
            entries: List<MediaPlays>,
            keep: PlayRetention,
        ): PlaysSummary {
            val duplicateIds = entries.flatMap { entry ->
                if (entry.plays.size <= 1) {
                    return@flatMap emptyList()
                }

                val chronological = entry.plays.sortedBy { it.watchedAt }
                val removable = when (keep) {
                    PlayRetention.Oldest -> chronological.drop(1)
                    PlayRetention.Newest -> chronological.dropLast(1)
                }
                removable.map { it.id }
            }

            return PlaysSummary(
                unique = entries.size,
                total = entries.sumOf { it.plays.size },
                duplicateIds = duplicateIds.toImmutableList(),
            )
        }
    }
}
