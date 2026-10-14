package tv.trakt.trakt.core.today.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.core.today.model.FollowingActivity

internal class TodayStorage : TodayLocalDataSource {
    private val mutex = Mutex()
    private var storage: List<FollowingActivity> = emptyList()

    override suspend fun setActivities(activities: List<FollowingActivity>) {
        mutex.withLock {
            storage = activities
        }
    }

    override suspend fun getActivities(): List<FollowingActivity> {
        return mutex.withLock {
            storage
        }
    }

    override fun clear() {
        storage = emptyList()
    }
}
