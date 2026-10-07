package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.networking.UserStatsDto

@Immutable
data class UserStats(
    val totalMinutes: Int,
    val totalPlays: Int,
) {
    companion object {
        fun fromDto(dto: UserStatsDto): UserStats {
            return UserStats(
                totalMinutes = dto.totalMinutes,
                totalPlays = dto.totalPlays,
            )
        }
    }
}
