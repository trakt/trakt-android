package tv.trakt.trakt.core.profile.sections.progress.data.local.completed

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.profile.ProfileProgressItem

internal class ProgressCompletedLocalSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<String, ProfileProgressItem>()

    suspend fun addItems(items: List<ProfileProgressItem>) {
        mutex.withLock {
            with(storage) {
                putAll(
                    items.associateBy {
                        getKey(it.id, it.type.value)
                    },
                )
            }
        }
    }

    suspend fun setItems(items: List<ProfileProgressItem>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(
                    items.associateBy {
                        getKey(it.id, it.type.value)
                    },
                )
            }
        }
    }

    suspend fun getItems(): List<ProfileProgressItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    fun clear() {
        storage.clear()
    }

    private fun getKey(
        id: TraktId,
        type: String,
    ): String {
        return "$type-$id"
    }
}
