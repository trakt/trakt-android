package tv.trakt.trakt.core.profile.sections.leaderboard.data.local

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.leaderboard.LeaderboardEntry

internal class LeaderboardStorage : LeaderboardLocalDataSource {
    private val mutex = Mutex()
    private var storage: ImmutableList<LeaderboardEntry>? = null

    override suspend fun setData(data: ImmutableList<LeaderboardEntry>) {
        mutex.withLock {
            storage = data
        }
    }

    override suspend fun getData(): ImmutableList<LeaderboardEntry>? {
        return mutex.withLock {
            storage
        }
    }

    override fun clear() {
        storage = null
    }
}
