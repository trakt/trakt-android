@file:OptIn(ExperimentalSerializationApi::class)

package tv.trakt.trakt.core.home.sections.recommended.local.shows

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
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem

private val KEY_RECOMMENDED_SHOWS = byteArrayPreferencesKey("key_recommended_shows")

internal class RecommendedShowsStorage(
    private val dataStore: DataStore<Preferences>,
) : RecommendedShowsLocalDataSource {
    private val mutex = Mutex()
    private var isInitialized = false

    private val showsCache = mutableMapOf<TraktId, RecommendedItem.ShowItem>()

    override suspend fun setShows(shows: List<RecommendedItem.ShowItem>) {
        ensureInitialized()
        mutex.withLock {
            with(showsCache) {
                clear()
                putAll(shows.associateBy { it.id })
            }

            dataStore.edit {
                it[KEY_RECOMMENDED_SHOWS] = ProtoBuf.encodeToByteArray(showsCache)
            }
        }
    }

    override suspend fun getShows(): List<RecommendedItem.ShowItem> {
        ensureInitialized()
        return mutex.withLock {
            showsCache.values.toList()
        }
    }

    override suspend fun removeShow(id: TraktId) {
        ensureInitialized()
        mutex.withLock {
            if (showsCache.remove(id) != null) {
                dataStore.edit {
                    it[KEY_RECOMMENDED_SHOWS] = ProtoBuf.encodeToByteArray(showsCache)
                }
            }
        }
    }

    override suspend fun clear() {
        ensureInitialized()
        mutex.withLock {
            showsCache.clear()
            dataStore.edit {
                it.remove(KEY_RECOMMENDED_SHOWS)
            }
        }
    }

    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            mutex.withLock {
                if (!isInitialized) {
                    try {
                        with(dataStore.data.first()) {
                            get(KEY_RECOMMENDED_SHOWS)?.let {
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
