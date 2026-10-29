package tv.trakt.trakt.common.model.leaderboard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.networking.api.v3.model.V3LeaderboardResponse

@Immutable
data class Leaderboard(
    val entries: ImmutableList<LeaderboardEntry>,
    val totalCount: Int?,
) {
    companion object {
        fun fromDto(dto: V3LeaderboardResponse): Leaderboard {
            return Leaderboard(
                entries = dto.entries
                    .map(LeaderboardEntry::fromDto)
                    .toImmutableList(),
                totalCount = dto.totalCount,
            )
        }
    }
}
