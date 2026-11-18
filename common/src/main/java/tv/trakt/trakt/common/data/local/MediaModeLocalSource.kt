package tv.trakt.trakt.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tv.trakt.trakt.common.model.MediaMode

class MediaModeLocalSource(
    private val dataStore: DataStore<Preferences>,
) {
    fun observeMode(): Flow<MediaMode> {
        return dataStore.data
            .map { prefs ->
                val value = prefs[KEY_MEDIA_MODE]
                MediaMode.entries.find { it.name == value } ?: MediaMode.Media
            }
            .distinctUntilChanged()
    }

    suspend fun setMode(mode: MediaMode) {
        dataStore.edit { it[KEY_MEDIA_MODE] = mode.name }
    }

    private companion object {
        val KEY_MEDIA_MODE = stringPreferencesKey("key_media_mode")
    }
}
