package tv.trakt.trakt.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

private val RETENTION = 3.days.toJavaDuration()

private val json = Json { ignoreUnknownKeys = true }

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
                    val entry = preferences.entry(key(locale, type, id), now) ?: return@mapNotNull null
                    id to entry.title
                }
                .toMap()
        }
    }

    /** Stores titles without overviews, keeping any cached overview that is still valid. */
    suspend fun putTitles(
        locale: String,
        titles: Map<MediaType, Map<TraktId, String?>>,
        now: Instant,
    ) {
        edit(locale, now) { preferences ->
            titles.forEach { (type, typeTitles) ->
                typeTitles.forEach { (id, title) ->
                    val key = key(locale, type, id)
                    if (preferences.entry(key, now)?.isComplete == true) return@forEach

                    preferences[key] = json.encodeToString(Entry(storedAt = now.toEpochMilli(), title = title))
                }
            }
        }
    }

    /**
     * The cached title and overview, or null when the overview was never fetched or is older
     * than the retention window. Both fields are null when the API has no translation.
     */
    suspend fun getTranslation(
        locale: String,
        type: MediaType,
        id: TraktId,
        now: Instant,
    ): MediaTranslation? {
        val entry = dataStore.data.first().entry(key(locale, type, id), now) ?: return null
        if (!entry.isComplete) return null

        return MediaTranslation(title = entry.title, overview = entry.overview)
    }

    suspend fun putTranslation(
        locale: String,
        type: MediaType,
        id: TraktId,
        translation: MediaTranslation,
        now: Instant,
    ) {
        edit(locale, now) { preferences ->
            preferences[key(locale, type, id)] = json.encodeToString(
                Entry(
                    storedAt = now.toEpochMilli(),
                    title = translation.title,
                    overview = translation.overview,
                    isComplete = true,
                ),
            )
        }
    }

    private suspend fun edit(
        locale: String,
        now: Instant,
        transform: (MutablePreferences) -> Unit,
    ) {
        val prune = prunedLocale.getAndSet(locale) != locale
        val localePrefix = "$locale:"

        dataStore.edit { preferences ->
            if (prune) {
                preferences
                    .asMap()
                    .filter { (key, stored) ->
                        !key.name.startsWith(localePrefix) || decode(stored as? String, now) == null
                    }
                    .keys
                    .forEach { preferences.remove(it) }
            }

            transform(preferences)
        }
    }

    private fun Preferences.entry(
        key: Preferences.Key<String>,
        now: Instant,
    ): Entry? {
        return decode(get(key), now)
    }

    private fun decode(
        value: String?,
        now: Instant,
    ): Entry? {
        if (value == null) return null

        val entry = runCatching { json.decodeFromString<Entry>(value) }.getOrNull() ?: return null
        if (Instant.ofEpochMilli(entry.storedAt).plus(RETENTION).isBefore(now)) return null

        return entry
    }

    private fun key(
        locale: String,
        type: MediaType,
        id: TraktId,
    ): Preferences.Key<String> {
        return stringPreferencesKey("$locale:${type.value}:${id.value}")
    }

    /** [isComplete] marks entries fetched with their overview, not only the title. */
    @Serializable
    private data class Entry(
        val storedAt: Long,
        val title: String? = null,
        val overview: String? = null,
        val isComplete: Boolean = false,
    )
}
