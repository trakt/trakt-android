package tv.trakt.trakt.core.profile.sections.progress.filters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.profile.sections.progress.model.ProgressFilter

private val KEY_PROGRESS_FILTER = stringPreferencesKey("key_profile_progress_filter")

internal class GetProgressFilterUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getFilter(): ProgressFilter {
        val storedFilter = dataStore.data.first()[KEY_PROGRESS_FILTER]
        return storedFilter?.let {
            runCatching { ProgressFilter.valueOf(it) }.getOrNull()
        } ?: ProgressFilter.Completed
    }

    suspend fun setFilter(filter: ProgressFilter) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_PROGRESS_FILTER] = filter.name
            }
        }
    }
}
