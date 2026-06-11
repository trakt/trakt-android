package tv.trakt.trakt.helpers.editscreen.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.helpers.editscreen.data.model.EditScreenKey
import java.util.concurrent.ConcurrentHashMap

internal class DefaultEditScreenManager(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : EditScreenManager {
    private val cache = ConcurrentHashMap<String, Boolean>()
    private val mutex = Mutex()

    init {
        scope.launch {
            val prefs = dataStore.data.first()
            EditScreenKey.entries.forEach { key ->
                cache[key.preferenceKey] = prefs[booleanPreferencesKey(key.preferenceKey)] ?: true
            }
        }
    }

    override fun isVisible(keys: Set<EditScreenKey>): Boolean {
        return keys.all { key ->
            cache[key.preferenceKey] ?: true
        }
    }

    override fun observe(keys: Set<EditScreenKey>): Flow<Map<EditScreenKey, Boolean>> =
        dataStore.data
            .map { prefs ->
                keys.associateWith { key -> prefs[booleanPreferencesKey(key.preferenceKey)] ?: true }
            }
            .distinctUntilChanged()

    override suspend fun hide(key: EditScreenKey) {
        mutex.withLock {
            cache[key.preferenceKey] = false
            dataStore.edit { prefs ->
                prefs[booleanPreferencesKey(key.preferenceKey)] = false
            }
        }
    }

    override suspend fun show(key: EditScreenKey) {
        mutex.withLock {
            cache[key.preferenceKey] = true
            dataStore.edit { prefs ->
                prefs[booleanPreferencesKey(key.preferenceKey)] = true
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            cache.clear()
            dataStore.edit { it.clear() }
        }
    }
}
