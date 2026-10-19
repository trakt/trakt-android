@file:OptIn(ExperimentalSerializationApi::class)

package tv.trakt.trakt.core.discover.sections.releases.data.local.shows

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
import tv.trakt.trakt.common.model.calendar.CalendarItem

private val KEY_RELEASES_SHOWS = byteArrayPreferencesKey("key_releases_shows")

internal class ReleasesShowsLocalSource(
    private val dataStore: DataStore<Preferences>,
) {
    private val mutex = Mutex()
    private var isInitialized = false

    private val showsCache = mutableMapOf<TraktId, CalendarItem.EpisodeItem>()

    suspend fun setItems(items: List<CalendarItem.EpisodeItem>) {
        ensureInitialized()
        mutex.withLock {
            with(showsCache) {
                clear()
                putAll(items.associateBy { it.id })
            }

            dataStore.edit {
                it[KEY_RELEASES_SHOWS] = ProtoBuf.encodeToByteArray(showsCache)
            }
        }
    }

    suspend fun getItems(): List<CalendarItem.EpisodeItem> {
        ensureInitialized()
        return mutex.withLock {
            showsCache.values.toList()
        }
    }

    suspend fun clear() {
        ensureInitialized()
        mutex.withLock {
            showsCache.clear()
            dataStore.edit { it.remove(KEY_RELEASES_SHOWS) }
        }
    }

    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            mutex.withLock {
                if (!isInitialized) {
                    try {
                        with(dataStore.data.first()) {
                            get(KEY_RELEASES_SHOWS)?.let {
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
