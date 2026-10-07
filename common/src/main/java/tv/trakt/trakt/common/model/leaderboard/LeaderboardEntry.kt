package tv.trakt.trakt.common.model.leaderboard

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.api.v3.model.V3LeaderboardEntryResponse

@Immutable
data class LeaderboardEntry(
    val user: User,
    val rank: Int?,
    val totalMinutes: Int?,
    val totalPlays: Int?,
    val isLocked: Boolean,
    val isViewer: Boolean = false,
) {
    companion object {
        fun fromDto(dto: V3LeaderboardEntryResponse): LeaderboardEntry {
            return LeaderboardEntry(
                user = User.fromDto(dto.user),
                rank = dto.rank,
                totalMinutes = dto.totalMinutes,
                totalPlays = dto.totalPlays,
                isLocked = dto.locked,
            )
        }
    }
}
