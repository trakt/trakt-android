@file:OptIn(ExperimentalSerializationApi::class)

package tv.trakt.trakt.core.discover.sections.anticipated.data.local.shows

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
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.model.DiscoverItem

private val KEY_ANTICIPATED_SHOWS = byteArrayPreferencesKey("key_anticipated_shows")

internal class AnticipatedShowsStorage(
    private val dataStore: DataStore<Preferences>,
) : AnticipatedShowsLocalDataSource {
    private val mutex = Mutex()
    private var isInitialized = false

    private val showsCache = mutableMapOf<TraktId, DiscoverItem.ShowItem>()

    override suspend fun setShows(shows: List<DiscoverItem.ShowItem>) {
        ensureInitialized()
        mutex.withLock {
            with(showsCache) {
                clear()
                putAll(shows.associateBy { it.id })
            }

            dataStore.edit {
                it[KEY_ANTICIPATED_SHOWS] = ProtoBuf.encodeToByteArray(showsCache)
            }
        }
    }

    override suspend fun getShows(): List<DiscoverItem.ShowItem> {
        ensureInitialized()
        return mutex.withLock {
            showsCache.values.toList()
        }
    }

    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            mutex.withLock {
                if (!isInitialized) {
                    try {
                        with(dataStore.data.first()) {
                            get(KEY_ANTICIPATED_SHOWS)?.let {
                                showsCache.putAll(ProtoBuf.decodeFromByteArray(it))
                            }
                        }
                    } catch (exception: SerializationException) {
                        showsCache.clear()
                        dataStore.edit { it.clear() }
                        Timber.e(exception)
                    } finally {
                        isInitialized = true
                    }
                }
            }
        }
    }
}
