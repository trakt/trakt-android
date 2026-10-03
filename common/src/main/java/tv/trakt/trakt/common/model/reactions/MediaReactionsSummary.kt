package tv.trakt.trakt.common.model.reactions

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3MediaReactionsSummaryResponse

@Immutable
data class MediaReactionsSummary(
    val reactionsCount: Int = 0,
    val usersCount: Int = 0,
    val distribution: ImmutableMap<MediaReaction, Int> = persistentMapOf(),
) {
    fun top(limit: Int): ImmutableList<MediaReaction> {
        return distribution
            .filterValues { it > 0 }
            .entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
            .toImmutableList()
    }

    companion object {
        fun fromDto(dto: V3MediaReactionsSummaryResponse): MediaReactionsSummary {
            return MediaReactionsSummary(
                reactionsCount = dto.reactionCount,
                usersCount = dto.userCount,
                distribution = MediaReaction.entries
                    .associateWith { dto.distribution[it.value] ?: 0 }
                    .toImmutableMap(),
            )
        }
    }
}
