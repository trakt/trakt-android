package tv.trakt.trakt.helpers.collapsing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class DefaultCollapsingManager(
    private val dataStore: DataStore<Preferences>,
) : CollapsingManager {
    private val cache = mutableMapOf<CollapsingKey, Boolean>()
    private val mutex = Mutex()

    init {
        runBlocking {
            val prefs = dataStore.data.first()
            CollapsingKey.entries.forEach { key ->
                val preferenceKey = booleanPreferencesKey(key.preferenceKey)
                cache[key] = prefs[preferenceKey] ?: false
            }
        }
    }

    override fun isCollapsed(key: CollapsingKey): Boolean {
        return cache[key] ?: false
    }

    override suspend fun collapse(key: CollapsingKey) {
        mutex.withLock {
            cache[key] = true
            dataStore.edit { prefs ->
                val preferenceKey = booleanPreferencesKey(key.preferenceKey)
                prefs[preferenceKey] = true
            }
        }
    }

    override suspend fun expand(key: CollapsingKey) {
        mutex.withLock {
            cache[key] = false
            dataStore.edit { prefs ->
                val preferenceKey = booleanPreferencesKey(key.preferenceKey)
                prefs[preferenceKey] = false
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            cache.clear()
            dataStore.edit {
                clear()
            }
        }
    }
}
