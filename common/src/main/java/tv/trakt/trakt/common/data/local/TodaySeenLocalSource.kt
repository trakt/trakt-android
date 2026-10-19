package tv.trakt.trakt.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

private val KEY_SEEN_STORIES = stringPreferencesKey("key_today_seen_stories")

// Matches the rolling Today window; older markers can no longer hide anything.
private val SEEN_RETENTION = 24.hours.toJavaDuration()

class TodaySeenLocalSource(
    private val dataStore: DataStore<Preferences>,
) {
    /** When each story group was last viewed, keyed by group key. */
    fun observeSeen(): Flow<Map<String, Instant>> {
        return dataStore.data.map { preferences ->
            decode(preferences[KEY_SEEN_STORIES])
                .mapValues { Instant.ofEpochMilli(it.value) }
        }
    }

    suspend fun markSeen(
        keys: Collection<String>,
        seenAt: Instant,
    ) {
        val cutoff = seenAt.minus(SEEN_RETENTION).toEpochMilli()

        dataStore.edit { preferences ->
            val seen = decode(preferences[KEY_SEEN_STORIES])
                .filterValues { it >= cutoff }
                .plus(keys.associateWith { seenAt.toEpochMilli() })

            preferences[KEY_SEEN_STORIES] = Json.encodeToString(seen)
        }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(KEY_SEEN_STORIES) }
    }

    private fun decode(value: String?): Map<String, Long> {
        if (value == null) return emptyMap()
        return runCatching { Json.decodeFromString<Map<String, Long>>(value) }
            .getOrDefault(emptyMap())
    }
}
