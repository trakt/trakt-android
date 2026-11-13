package tv.trakt.trakt.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

// Manifests rarely change, and a stale entry only delays a new localized poster.
private val RETENTION = 3.days.toJavaDuration()

private const val TIME_SEPARATOR = ':'
private const val LANGUAGE_SEPARATOR = ","

class PosterLanguagesLocalSource(
    private val dataStore: DataStore<Preferences>,
) {
    // Expired entries are only dropped on the first write of each session; reads already
    // ignore them, so later writes skip the full scan.
    private val isPruneDue = AtomicBoolean(true)

    /** Poster languages for a title, or null when unknown or older than the retention window. */
    suspend fun getLanguages(
        key: String,
        now: Instant,
    ): Set<String>? {
        val value = dataStore.data.first()[stringPreferencesKey(key)] ?: return null
        if (isExpired(value, now)) return null

        return value
            .substringAfter(TIME_SEPARATOR)
            .split(LANGUAGE_SEPARATOR)
            .filter { it.isNotBlank() }
            .toSet()
    }

    suspend fun putLanguages(
        key: String,
        languages: Set<String>,
        now: Instant,
    ) {
        val value = "${now.toEpochMilli()}$TIME_SEPARATOR${languages.joinToString(LANGUAGE_SEPARATOR)}"

        val prune = isPruneDue.compareAndSet(true, false)

        dataStore.edit { preferences ->
            if (prune) {
                preferences
                    .asMap()
                    .filter { (_, stored) -> isExpired(stored as? String, now) }
                    .keys
                    .forEach { preferences.remove(it) }
            }

            preferences[stringPreferencesKey(key)] = value
        }
    }

    private fun isExpired(
        value: String?,
        now: Instant,
    ): Boolean {
        val storedAt = value
            ?.substringBefore(TIME_SEPARATOR)
            ?.toLongOrNull()
            ?: return true

        return Instant.ofEpochMilli(storedAt).plus(RETENTION).isBefore(now)
    }
}
