package tv.trakt.trakt.core.profile.sections.favorites.filters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.main.model.MediaMode

private val KEY_ACTIVITY_FILTER = stringPreferencesKey("key_profile_favorites_filter")

internal class GetFavoritesFilterUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getFilter(): MediaMode {
        val storedFilter = dataStore.data.first()[KEY_ACTIVITY_FILTER]
        return storedFilter?.let {
            MediaMode.valueOf(it)
        } ?: MediaMode.MEDIA
    }

    suspend fun setFilter(filter: MediaMode) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_ACTIVITY_FILTER] = filter.name
            }
        }
    }
}
