package tv.trakt.trakt.core.profile.sections.activity.data.local.ratings

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem

internal class ProfileRatingsStorage : ProfileRatingsLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<String, ProfileRatingItem>()

    override suspend fun addItems(items: List<ProfileRatingItem>) {
        mutex.withLock {
            with(storage) {
                putAll(items.associateBy { it.key })
            }
        }
    }

    override suspend fun setItems(items: List<ProfileRatingItem>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.key })
            }
        }
    }

    override suspend fun getItems(): List<ProfileRatingItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            storage.clear()
        }
    }
}
