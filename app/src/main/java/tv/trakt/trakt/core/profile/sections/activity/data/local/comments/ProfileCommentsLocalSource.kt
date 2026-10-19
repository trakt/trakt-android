package tv.trakt.trakt.core.profile.sections.activity.data.local.comments

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.profile.ProfileCommentItem

internal class ProfileCommentsLocalSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<String, ProfileCommentItem>()

    suspend fun addItems(items: List<ProfileCommentItem>) {
        mutex.withLock {
            with(storage) {
                putAll(items.associateBy { it.key })
            }
        }
    }

    suspend fun setItems(items: List<ProfileCommentItem>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.key })
            }
        }
    }

    suspend fun getItems(): List<ProfileCommentItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    suspend fun clear() {
        mutex.withLock {
            storage.clear()
        }
    }
}
