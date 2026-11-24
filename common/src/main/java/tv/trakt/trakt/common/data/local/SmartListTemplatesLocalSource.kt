package tv.trakt.trakt.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.common.model.lists.SmartListTemplate

private val KEY_SMART_LIST_TEMPLATES = stringPreferencesKey("key_smart_list_templates")

// ProtoBuf cannot encode the null filter lists, so templates are stored as JSON.
private val json = Json { ignoreUnknownKeys = true }

class SmartListTemplatesLocalSource(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getTemplates(): ImmutableList<SmartListTemplate>? {
        val value = dataStore.data.first()[KEY_SMART_LIST_TEMPLATES] ?: return null

        return try {
            json.decodeFromString<List<SmartListTemplate>>(value).toImmutableList()
        } catch (exception: SerializationException) {
            Timber.e(exception)
            clear()
            null
        }
    }

    suspend fun setTemplates(templates: List<SmartListTemplate>) {
        dataStore.edit {
            it[KEY_SMART_LIST_TEMPLATES] = json.encodeToString(templates)
        }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(KEY_SMART_LIST_TEMPLATES) }
    }
}
