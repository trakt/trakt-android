package tv.trakt.trakt.app.core.tutorials

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.app.core.tutorials.model.TutorialKey
import tv.trakt.trakt.app.core.tutorials.model.TutorialKey.WATCH_NOW_MORE

@Suppress("UNCHECKED_CAST")
internal class DefaultTutorialsManager(
    private val dataStore: DataStore<Preferences>,
) : TutorialsManager {
    override suspend fun get(key: TutorialKey): Boolean {
        val data = dataStore.data.first()
        return when (key) {
            WATCH_NOW_MORE -> data[booleanPreferencesKey(key.preferenceKey)] ?: false
        }
    }

    override suspend fun acknowledge(key: TutorialKey) {
        dataStore.edit {
            when (key) {
                WATCH_NOW_MORE -> it[booleanPreferencesKey(key.preferenceKey)] = true
            }
        }
    }
}
