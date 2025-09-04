package tv.trakt.trakt.core.search.data.local

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
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.search.data.local.model.RecentMovieEntity
import tv.trakt.trakt.core.search.data.local.model.RecentPersonEntity
import tv.trakt.trakt.core.search.data.local.model.RecentShowEntity
import tv.trakt.trakt.core.search.data.local.model.create
import java.time.Instant

private val KEY_RECENT_SEARCH_SHOWS = byteArrayPreferencesKey("key_recent_search_shows")
private val KEY_RECENT_SEARCH_MOVIES = byteArrayPreferencesKey("key_recent_search_movies")
private val KEY_RECENT_SEARCH_PEOPLE = byteArrayPreferencesKey("key_recent_search_people")

@OptIn(ExperimentalSerializationApi::class)
internal class RecentSearchStorage(
    private val dataStore: DataStore<Preferences>,
) : RecentSearchLocalDataSource {
    private val mutex = Mutex()
    private var isInitialized = false

    private val showsCache = mutableMapOf<TraktId, RecentShowEntity>()
    private val moviesCache = mutableMapOf<TraktId, RecentMovieEntity>()
    private val personsCache = mutableMapOf<TraktId, RecentPersonEntity>()

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

    override suspend fun addPerson(
        person: Person,
        limit: Int,
        addedAt: Instant,
    ) {
        ensureInitialized()
        val entity = RecentPersonEntity.create(
            person = person,
            createdAt = addedAt,
        )
        mutex.withLock {
            if (personsCache.size >= limit) {
                personsCache.values.minByOrNull { it.createdAt }?.let {
                    personsCache.remove(it.person.ids.trakt)
                }
            }
            personsCache[person.ids.trakt] = entity
            dataStore.edit {
                it[KEY_RECENT_SEARCH_PEOPLE] = ProtoBuf.encodeToByteArray(personsCache)
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

    override suspend fun getPeople(): List<RecentPersonEntity> {
        ensureInitialized()
        return mutex.withLock {
            personsCache.values.sortedByDescending { it.createdAt }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            showsCache.clear()
            moviesCache.clear()
            personsCache.clear()
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
                        get(KEY_RECENT_SEARCH_PEOPLE)?.let {
                            personsCache.putAll(ProtoBuf.decodeFromByteArray(it))
                        }
                    }
                }
            }
        }
    }
}
