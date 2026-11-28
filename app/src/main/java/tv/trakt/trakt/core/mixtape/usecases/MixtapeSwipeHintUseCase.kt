package tv.trakt.trakt.core.mixtape.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.first

private val KEY_MIXTAPE_SWIPE_HINT_SEEN = booleanPreferencesKey("key_mixtape_swipe_hint_seen_2")

internal class MixtapeSwipeHintUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun isHintPending(): Boolean {
        return dataStore.data.first()[KEY_MIXTAPE_SWIPE_HINT_SEEN] != true
    }

    suspend fun dismissHint() {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_MIXTAPE_SWIPE_HINT_SEEN] = true
            }
        }
    }
}
