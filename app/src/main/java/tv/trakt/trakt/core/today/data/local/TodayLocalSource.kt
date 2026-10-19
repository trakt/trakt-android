package tv.trakt.trakt.core.today.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.core.today.model.FollowingActivity
import tv.trakt.trakt.common.core.today.model.TodayForYouItem

internal class TodayLocalSource {
    private val mutex = Mutex()
    private var storage: List<FollowingActivity> = emptyList()
    private var forYouStorage: List<TodayForYouItem> = emptyList()

    // Watched items stay hidden until the next fetch, which no longer returns them.
    private var watchedForYouKeys: Set<String> = emptySet()

    suspend fun setActivities(activities: List<FollowingActivity>) {
        mutex.withLock {
            storage = activities
        }
    }

    suspend fun getActivities(): List<FollowingActivity> {
        return mutex.withLock {
            storage
        }
    }

    suspend fun setForYou(items: List<TodayForYouItem>) {
        mutex.withLock {
            forYouStorage = items
            watchedForYouKeys = emptySet()
        }
    }

    suspend fun getForYou(): List<TodayForYouItem> {
        return mutex.withLock {
            forYouStorage.filterNot { it.key in watchedForYouKeys }
        }
    }

    suspend fun setForYouWatched(
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

    fun clear() {
        storage = emptyList()
        forYouStorage = emptyList()
        watchedForYouKeys = emptySet()
    }
}
