package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.core.library.LibraryItem

class UserLibraryLocalSource {
    private val mutex = Mutex()

    private var storage: MutableMap<String, LibraryItem>? = null

    suspend fun setItems(items: List<LibraryItem>) {
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

    suspend fun addItems(items: List<LibraryItem>) {
        mutex.withLock {
            if (storage == null) {
                storage = mutableMapOf()
            }
            storage?.let { storage ->
                storage.putAll(items.associateBy { it.key })
            }
        }
    }

    suspend fun isLoaded(): Boolean {
        return mutex.withLock {
            storage != null
        }
    }

    suspend fun getAll(): List<LibraryItem> {
        return mutex.withLock {
            storage?.values?.toList() ?: emptyList()
        }
    }

    fun clear() {
        storage?.clear()
        storage = null
    }
}
