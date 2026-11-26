package tv.trakt.trakt.core.mixtape.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val KEY_MIXTAPE_HINT_SEEN = booleanPreferencesKey("key_mixtape_hint_seen")

internal class MixtapeHintUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    fun observeHint(): Flow<Boolean> {
        return dataStore.data
            .map { preferences -> preferences[KEY_MIXTAPE_HINT_SEEN] != true }
            .distinctUntilChanged()
    }

    suspend fun dismissHint() {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_MIXTAPE_HINT_SEEN] = true
            }
        }
    }
}
