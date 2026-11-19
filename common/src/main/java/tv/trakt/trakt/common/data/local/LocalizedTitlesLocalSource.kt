package tv.trakt.trakt.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

private const val TIME_SEPARATOR = ':'
private val RETENTION = 3.days.toJavaDuration()

class LocalizedTitlesLocalSource(
    private val dataStore: DataStore<Preferences>,
) {
    // Expired and other-locale entries are only dropped on the first write of each session or
    // locale; reads already ignore them, so later writes skip the full scan.
    private val prunedLocale = AtomicReference<String?>(null)

    /**
     * Cached titles per type and id. A null title means the API has no translation for that id.
     * Ids that are unknown or older than the retention window are left out.
     */
    suspend fun getTitles(
        locale: String,
        ids: Map<MediaType, Collection<TraktId>>,
        now: Instant,
    ): Map<MediaType, Map<TraktId, String?>> {
        val preferences = dataStore.data.first()

        return ids.mapValues { (type, typeIds) ->
            typeIds
                .mapNotNull { id ->
                    val value = preferences[key(locale, type, id)]
                    if (isExpired(value, now)) return@mapNotNull null

                    id to value?.substringAfter(TIME_SEPARATOR)?.ifEmpty { null }
                }
                .toMap()
        }
    }

    suspend fun putTitles(
        locale: String,
        titles: Map<MediaType, Map<TraktId, String?>>,
        now: Instant,
    ) {
        val prune = prunedLocale.getAndSet(locale) != locale
        val localePrefix = "$locale:"

        dataStore.edit { preferences ->
            if (prune) {
                preferences
                    .asMap()
                    .filter { (key, stored) ->
                        !key.name.startsWith(localePrefix) || isExpired(stored as? String, now)
                    }
                    .keys
                    .forEach { preferences.remove(it) }
            }

            titles.forEach { (type, typeTitles) ->
                typeTitles.forEach { (id, title) ->
                    preferences[key(locale, type, id)] = "${now.toEpochMilli()}$TIME_SEPARATOR${title.orEmpty()}"
                }
            }
        }
    }

    private fun key(
        locale: String,
        type: MediaType,
        id: TraktId,
    ): Preferences.Key<String> {
        return stringPreferencesKey("$locale:${type.value}:${id.value}")
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
