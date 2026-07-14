@file:OptIn(ExperimentalSerializationApi::class)

package tv.trakt.trakt.core.discover.sections.releases.data.local.movies

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
import tv.trakt.trakt.core.calendar.model.CalendarItem

private val KEY_RELEASES_MOVIES = byteArrayPreferencesKey("key_releases_movies")

internal class ReleasesMoviesStorage(
    private val dataStore: DataStore<Preferences>,
) : ReleasesMoviesLocalDataSource {
    private val mutex = Mutex()
    private var isInitialized = false

    private val moviesCache = mutableMapOf<TraktId, CalendarItem.MovieItem>()

    override suspend fun setMovies(movies: List<CalendarItem.MovieItem>) {
        ensureInitialized()
        mutex.withLock {
            with(moviesCache) {
                clear()
                putAll(movies.associateBy { it.id })
            }

            dataStore.edit {
                it[KEY_RELEASES_MOVIES] = ProtoBuf.encodeToByteArray(moviesCache)
            }
        }
    }

    override suspend fun getMovies(): List<CalendarItem.MovieItem> {
        ensureInitialized()
        return mutex.withLock {
            moviesCache.values.toList()
        }
    }

    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            mutex.withLock {
                if (!isInitialized) {
                    try {
                        with(dataStore.data.first()) {
                            get(KEY_RELEASES_MOVIES)?.let {
                                moviesCache.putAll(ProtoBuf.decodeFromByteArray(it))
                            }
                        }
                    } catch (exception: SerializationException) {
                        moviesCache.clear()
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
