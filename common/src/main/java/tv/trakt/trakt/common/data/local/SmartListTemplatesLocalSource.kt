package tv.trakt.trakt.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.common.model.lists.SmartListTemplate
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

private val KEY_SMART_LIST_TEMPLATES = stringPreferencesKey("key_smart_list_templates")
private val KEY_SMART_LIST_TEMPLATES_STORED_AT = longPreferencesKey("key_smart_list_templates_stored_at")

private val RETENTION = 7.days.toJavaDuration()

// ProtoBuf cannot encode the null filter lists, so templates are stored as JSON.
private val json = Json { ignoreUnknownKeys = true }

class SmartListTemplatesLocalSource(
    private val dataStore: DataStore<Preferences>,
) {
    /** Cached templates, or null when missing or older than the retention window. */
    suspend fun getTemplates(now: Instant): ImmutableList<SmartListTemplate>? {
        val preferences = dataStore.data.first()
        val storedAt = preferences[KEY_SMART_LIST_TEMPLATES_STORED_AT] ?: return null
        if (Instant.ofEpochMilli(storedAt).plus(RETENTION).isBefore(now)) return null

        val value = preferences[KEY_SMART_LIST_TEMPLATES] ?: return null

        return try {
            json.decodeFromString<List<SmartListTemplate>>(value).toImmutableList()
        } catch (exception: SerializationException) {
            Timber.e(exception)
            clear()
            null
        }
    }

    suspend fun setTemplates(
        templates: List<SmartListTemplate>,
        now: Instant,
    ) {
        dataStore.edit {
            it[KEY_SMART_LIST_TEMPLATES] = json.encodeToString(templates)
            it[KEY_SMART_LIST_TEMPLATES_STORED_AT] = now.toEpochMilli()
        }
    }

    suspend fun clear() {
        dataStore.edit {
            it.remove(KEY_SMART_LIST_TEMPLATES)
            it.remove(KEY_SMART_LIST_TEMPLATES_STORED_AT)
        }
    }
}
