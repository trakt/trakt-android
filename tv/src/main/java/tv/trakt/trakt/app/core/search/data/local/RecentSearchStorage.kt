package tv.trakt.trakt.app.core.search.data.local

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
import tv.trakt.trakt.app.common.model.TraktId
import tv.trakt.trakt.app.core.movies.model.Movie
import tv.trakt.trakt.app.core.search.data.local.model.RecentMovieEntity
import tv.trakt.trakt.app.core.search.data.local.model.RecentShowEntity
import tv.trakt.trakt.app.core.search.data.local.model.create
import tv.trakt.trakt.app.core.shows.model.Show
import java.time.Instant

private val KEY_RECENT_SEARCH_SHOWS = byteArrayPreferencesKey("key_recent_search_shows")
private val KEY_RECENT_SEARCH_MOVIES = byteArrayPreferencesKey("key_recent_search_movies")

@OptIn(ExperimentalSerializationApi::class)
internal class RecentSearchStorage(
    private val dataStore: DataStore<Preferences>,
) : RecentSearchLocalDataSource {
    private val mutex = Mutex()
    private var isInitialized = false

    private val showsCache = mutableMapOf<TraktId, RecentShowEntity>()
    private val moviesCache = mutableMapOf<TraktId, RecentMovieEntity>()

    override suspend fun addShow(
        show: Show,
        limit: Int,
        addedAt: Instant,
    ) {
        ensureInitialized()
        val entity = RecentShowEntity.create(
            show = show,
            createdAt = addedAt,
        )
        mutex.withLock {
            if (showsCache.size >= limit) {
                showsCache.values.minByOrNull { it.createdAt }?.let {
                    showsCache.remove(it.show.ids.trakt)
                }
            }
            showsCache[show.ids.trakt] = entity
            dataStore.edit {
                it[KEY_RECENT_SEARCH_SHOWS] = ProtoBuf.encodeToByteArray(showsCache)
            }
        }
    }

    override suspend fun addMovie(
        movie: Movie,
        limit: Int,
        addedAt: Instant,
    ) {
        ensureInitialized()
        val entity = RecentMovieEntity.create(
            movie = movie,
            createdAt = addedAt,
        )
        mutex.withLock {
            if (moviesCache.size >= limit) {
                moviesCache.values.minByOrNull { it.createdAt }?.let {
                    moviesCache.remove(it.movie.ids.trakt)
                }
            }
            moviesCache[movie.ids.trakt] = entity
            dataStore.edit {
                it[KEY_RECENT_SEARCH_MOVIES] = ProtoBuf.encodeToByteArray(moviesCache)
            }
        }
    }

    override suspend fun getShows(): List<RecentShowEntity> {
        ensureInitialized()
        return mutex.withLock {
            showsCache.values.sortedByDescending { it.createdAt }
        }
    }

    override suspend fun getMovies(): List<RecentMovieEntity> {
        ensureInitialized()
        return mutex.withLock {
            moviesCache.values.sortedByDescending { it.createdAt }
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
                        get(KEY_RECENT_SEARCH_SHOWS)?.let {
                            showsCache.putAll(ProtoBuf.decodeFromByteArray(it))
                        }
                        get(KEY_RECENT_SEARCH_MOVIES)?.let {
                            moviesCache.putAll(ProtoBuf.decodeFromByteArray(it))
                        }
                    }
                }
            }
        }
    }
}
