package tv.trakt.trakt.core.search.data.local.people

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
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.search.data.local.model.PersonEntity
import java.time.Instant

private val KEY_BIRTHDAY_SEARCH_PEOPLE = byteArrayPreferencesKey("key_birthday_search_people")

@OptIn(ExperimentalSerializationApi::class)
internal class SearchPeopleStorage(
    private val dataStore: DataStore<Preferences>,
) : SearchPeopleLocalDataSource {
    private val mutex = Mutex()
    private var isInitialized = false

    private val cache = mutableMapOf<TraktId, PersonEntity>()

    override suspend fun setPeople(
        people: List<Person>,
        createdAt: Instant,
    ) {
        ensureInitialized()
        mutex.withLock {
            with(cache) {
                val entities = people.associateBy(
                    keySelector = { it.ids.trakt },
                    valueTransform = {
                        PersonEntity(
                            person = it,
                            createdAt = createdAt.toString(),
                        )
                    },
                )
                clear()
                putAll(entities)
            }

            dataStore.edit {
                it[KEY_BIRTHDAY_SEARCH_PEOPLE] = ProtoBuf.encodeToByteArray(cache)
            }
        }
    }

    override suspend fun getPeople(): List<Person> {
        ensureInitialized()
        return mutex.withLock {
            cache.values.map { it.person }
        }
    }

    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            mutex.withLock {
                if (!isInitialized) {
                    with(dataStore.data.first()) {
                        get(KEY_BIRTHDAY_SEARCH_PEOPLE)?.let {
                            cache.putAll(ProtoBuf.decodeFromByteArray(it))
                        }
                    }
                }
            }
        }
    }
}
