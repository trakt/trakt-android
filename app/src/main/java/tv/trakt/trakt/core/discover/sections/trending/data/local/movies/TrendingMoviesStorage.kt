@file:OptIn(ExperimentalSerializationApi::class)

package tv.trakt.trakt.core.discover.sections.trending.data.local.movies

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

private val KEY_TRENDING_MOVIES = byteArrayPreferencesKey("key_trending_movies")

internal class TrendingMoviesStorage(
    private val dataStore: DataStore<Preferences>,
) : TrendingMoviesLocalDataSource {
    private val mutex = Mutex()
    private var isInitialized = false

    private val moviesCache = mutableMapOf<TraktId, DiscoverItem.MovieItem>()

    override suspend fun setMovies(movies: List<DiscoverItem.MovieItem>) {
        ensureInitialized()
        mutex.withLock {
            with(moviesCache) {
                clear()
                putAll(movies.associateBy { it.id })
            }

            dataStore.edit {
                it[KEY_TRENDING_MOVIES] = ProtoBuf.encodeToByteArray(moviesCache)
            }
        }
    }

    override suspend fun getMovies(): List<DiscoverItem.MovieItem> {
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
                            get(KEY_TRENDING_MOVIES)?.let {
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
