package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.leaderboard.Leaderboard

class LeaderboardLocalSource {
    private val mutex = Mutex()
    private var storage: Leaderboard? = null

    suspend fun setData(data: Leaderboard) {
        mutex.withLock {
            storage = data
        }
    }

    suspend fun getData(): Leaderboard? {
        return mutex.withLock {
            storage
        }
    }

    fun clear() {
        storage = null
    }
}
