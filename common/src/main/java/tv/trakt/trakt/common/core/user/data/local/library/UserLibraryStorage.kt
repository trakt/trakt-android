package tv.trakt.trakt.common.core.user.data.local.library

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.core.library.LibraryItem

class UserLibraryStorage : UserLibraryLocalDataSource {
    private val mutex = Mutex()

    private var storage: MutableMap<String, LibraryItem>? = null

    override suspend fun setItems(items: List<LibraryItem>) {
        mutex.withLock {
            if (storage == null) {
                storage = mutableMapOf()
            }
            storage?.let { storage ->
                storage.clear()
                storage.putAll(items.associateBy { it.key })
            }
        }
    }

    override suspend fun addItems(items: List<LibraryItem>) {
        mutex.withLock {
            if (storage == null) {
                storage = mutableMapOf()
            }
            storage?.let { storage ->
                storage.putAll(items.associateBy { it.key })
            }
        }
    }

    override suspend fun isLoaded(): Boolean {
        return mutex.withLock {
            storage != null
        }
    }

    override suspend fun getAll(): List<LibraryItem> {
        return mutex.withLock {
            storage?.values?.toList() ?: emptyList()
        }
    }

    override fun clear() {
        storage?.clear()
        storage = null
    }
}
