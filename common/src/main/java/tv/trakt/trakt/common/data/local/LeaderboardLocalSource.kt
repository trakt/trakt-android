package tv.trakt.trakt.common.data.local

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.leaderboard.LeaderboardEntry

class LeaderboardLocalSource {
    private val mutex = Mutex()
    private var storage: ImmutableList<LeaderboardEntry>? = null

    suspend fun setData(data: ImmutableList<LeaderboardEntry>) {
        mutex.withLock {
            storage = data
        }
    }

    suspend fun getData(): ImmutableList<LeaderboardEntry>? {
        return mutex.withLock {
            storage
        }
    }

    fun clear() {
        storage = null
    }
}
