package tv.trakt.trakt.core.lists.features.convert.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.common.model.TraktId
import java.time.Duration
import java.time.Instant

private val KEY_UPGRADE_DISMISSED = stringSetPreferencesKey("key_smart_list_upgrade_dismissed")
private const val KEY_NUDGE_DISMISSED_PREFIX = "key_smart_list_nudge_dismissed_"

private val NUDGE_SNOOZE = Duration.ofDays(14)

internal class SmartListDismissalsUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun isUpgradeDismissed(listId: TraktId): Boolean {
        val dismissed = dataStore.data.first()[KEY_UPGRADE_DISMISSED].orEmpty()
        return listId.value.toString() in dismissed
    }

    suspend fun dismissUpgrade(listId: TraktId) {
        dataStore.edit { prefs ->
            prefs[KEY_UPGRADE_DISMISSED] = prefs[KEY_UPGRADE_DISMISSED].orEmpty() + listId.value.toString()
        }
    }

    suspend fun isNudgeDismissed(
        userId: TraktId,
        now: Instant,
    ): Boolean {
        val dismissedAt = dataStore.data.first()[nudgeKey(userId)] ?: return false
        return Instant.ofEpochMilli(dismissedAt).plus(NUDGE_SNOOZE).isAfter(now)
    }

    suspend fun dismissNudge(
        userId: TraktId,
        now: Instant,
    ) {
        dataStore.edit { prefs ->
            prefs[nudgeKey(userId)] = now.toEpochMilli()
        }
    }

    private fun nudgeKey(userId: TraktId): Preferences.Key<Long> {
        return longPreferencesKey("$KEY_NUDGE_DISMISSED_PREFIX${userId.value}")
    }
}
