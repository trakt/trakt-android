@file:OptIn(ExperimentalSerializationApi::class)

package tv.trakt.trakt.core.discover.data.local.media

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import timber.log.Timber
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverSection

internal class DiscoverMediaStorage(
    private val dataStore: DataStore<Preferences>,
) : DiscoverMediaLocalDataSource {
    private val mutex = Mutex()
    private val cache = mutableMapOf<DiscoverSection, List<DiscoverItem>>()

    override suspend fun setItems(
        section: DiscoverSection,
        items: List<DiscoverItem>,
    ) {
        mutex.withLock {
            cache[section] = items
            dataStore.edit {
                it[section.storageKey] = ProtoBuf.encodeToByteArray(items)
            }
        }
    }

    override suspend fun getItems(section: DiscoverSection): List<DiscoverItem> {
        return mutex.withLock {
            cache.getOrPut(section) { readItems(section) }
        }
    }

    private suspend fun readItems(section: DiscoverSection): List<DiscoverItem> {
        return try {
            dataStore.data.first()[section.storageKey]
                ?.let { ProtoBuf.decodeFromByteArray<List<DiscoverItem>>(it) }
                ?: emptyList()
        } catch (exception: SerializationException) {
            dataStore.edit { it.remove(section.storageKey) }
            Timber.e(exception)
            emptyList()
        }
    }
}

private val DiscoverSection.storageKey: Preferences.Key<ByteArray>
    get() = byteArrayPreferencesKey("key_discover_media_${name.lowercase()}")
