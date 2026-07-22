package tv.trakt.trakt.core.discover.sections.releases.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.ReleaseType

private val KEY_RELEASES_TYPE = stringPreferencesKey("key_discover_releases_type")

internal class GetReleasesTypeUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getType(): ReleaseType {
        val storedType = dataStore.data.first()[KEY_RELEASES_TYPE]
        return storedType?.let {
            runCatching { ReleaseType.valueOf(it) }.getOrNull()
        } ?: ReleaseType.All
    }

    suspend fun setType(type: ReleaseType) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_RELEASES_TYPE] = type.name
            }
        }
    }
}
