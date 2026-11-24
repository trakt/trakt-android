package tv.trakt.trakt.core.lists.sections.smart.templates.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val KEY_SMART_LIST_TEMPLATES_HIDDEN = booleanPreferencesKey("key_smart_list_templates_hidden")

internal class SmartListTemplatesHiddenUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    fun observeHidden(): Flow<Boolean> {
        return dataStore.data.map { it[KEY_SMART_LIST_TEMPLATES_HIDDEN] ?: false }
    }

    suspend fun hide() {
        dataStore.edit { it[KEY_SMART_LIST_TEMPLATES_HIDDEN] = true }
    }
}
