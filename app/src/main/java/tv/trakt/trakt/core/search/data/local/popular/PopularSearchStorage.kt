package tv.trakt.trakt.core.search.data.local.popular

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import tv.trakt.trakt.core.search.data.local.model.PopularMovieEntity
import tv.trakt.trakt.core.search.data.local.model.PopularShowEntity

private val KEY_POPULAR_SEARCH_SHOWS = byteArrayPreferencesKey("key_popular_search_shows")
private val KEY_POPULAR_SEARCH_MOVIES = byteArrayPreferencesKey("key_popular_search_movies")

@OptIn(ExperimentalSerializationApi::class)
internal class PopularSearchStorage(
    private val dataStore: DataStore<Preferences>,
) : PopularSearchLocalDataSource {
    private val mutex = Mutex()
    private var isInitialized = false

    private val showsCache = mutableMapOf<Int, PopularShowEntity>()
    private val moviesCache = mutableMapOf<Int, PopularMovieEntity>()

    override suspend fun setShows(shows: List<PopularShowEntity>) {
        ensureInitialized()
        mutex.withLock {
            showsCache.clear()
            shows.associateByTo(showsCache) {
                it.show.ids.trakt.value
            }

            dataStore.edit {
                it[KEY_POPULAR_SEARCH_SHOWS] = ProtoBuf.encodeToByteArray(showsCache)
            }
        }
    }

    override suspend fun setMovies(movies: List<PopularMovieEntity>) {
        ensureInitialized()
        mutex.withLock {
            moviesCache.clear()
            movies.associateByTo(moviesCache) {
                it.movie.ids.trakt.value
            }

            dataStore.edit {
                it[KEY_POPULAR_SEARCH_MOVIES] = ProtoBuf.encodeToByteArray(moviesCache)
            }
        }
    }

    override suspend fun getShows(): List<PopularShowEntity> {
        ensureInitialized()
        return mutex.withLock {
            showsCache.values.toList()
        }
    }

    override suspend fun getMovies(): List<PopularMovieEntity> {
        ensureInitialized()
        return mutex.withLock {
            moviesCache.values.toList()
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            showsCache.clear()
            moviesCache.clear()
            dataStore.edit { it.clear() }
        }
    }

    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            mutex.withLock {
                if (!isInitialized) {
                    with(dataStore.data.first()) {
                        get(KEY_POPULAR_SEARCH_SHOWS)?.let {
                            showsCache.putAll(ProtoBuf.decodeFromByteArray(it))
                        }
                        get(KEY_POPULAR_SEARCH_MOVIES)?.let {
                            moviesCache.putAll(ProtoBuf.decodeFromByteArray(it))
                        }
                    }
                }
            }
        }
    }
}
