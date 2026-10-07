package tv.trakt.trakt.core.profile.sections.leaderboard.data.local

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.leaderboard.LeaderboardEntry

internal interface LeaderboardLocalDataSource {
    suspend fun setData(data: ImmutableList<LeaderboardEntry>)

    suspend fun getData(): ImmutableList<LeaderboardEntry>?

    fun clear()
}
