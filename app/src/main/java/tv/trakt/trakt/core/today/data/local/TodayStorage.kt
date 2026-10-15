package tv.trakt.trakt.core.today.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.core.today.model.FollowingActivity
import tv.trakt.trakt.core.today.model.TodayForYouItem

internal class TodayStorage : TodayLocalDataSource {
    private val mutex = Mutex()
    private var storage: List<FollowingActivity> = emptyList()
    private var forYouStorage: List<TodayForYouItem> = emptyList()

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

    override suspend fun setForYou(items: List<TodayForYouItem>) {
        mutex.withLock {
            forYouStorage = items
        }
    }

    override suspend fun getForYou(): List<TodayForYouItem> {
        return mutex.withLock {
            forYouStorage
        }
    }

    override fun clear() {
        storage = emptyList()
        forYouStorage = emptyList()
    }
}
