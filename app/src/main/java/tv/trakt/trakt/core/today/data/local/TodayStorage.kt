package tv.trakt.trakt.core.today.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.core.today.model.FollowingActivity
import tv.trakt.trakt.core.today.model.TodayForYouItem

internal class TodayStorage : TodayLocalDataSource {
    private val mutex = Mutex()
    private var storage: List<FollowingActivity> = emptyList()
    private var forYouStorage: List<TodayForYouItem> = emptyList()

    // Watched items stay hidden until the next fetch, which no longer returns them.
    private var watchedForYouKeys: Set<String> = emptySet()

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
            watchedForYouKeys = emptySet()
        }
    }

    override suspend fun getForYou(): List<TodayForYouItem> {
        return mutex.withLock {
            forYouStorage.filterNot { it.key in watchedForYouKeys }
        }
    }

    override suspend fun setForYouWatched(
        key: String,
        watched: Boolean,
    ) {
        mutex.withLock {
            watchedForYouKeys = when {
                watched -> watchedForYouKeys + key
                else -> watchedForYouKeys - key
            }
        }
    }

    override fun clear() {
        storage = emptyList()
        forYouStorage = emptyList()
        watchedForYouKeys = emptySet()
    }
}
