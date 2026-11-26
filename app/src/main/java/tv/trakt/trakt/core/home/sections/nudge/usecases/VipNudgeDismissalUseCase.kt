package tv.trakt.trakt.core.home.sections.nudge.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.common.model.TraktId
import java.time.Duration
import java.time.Instant

private const val KEY_VIP_NUDGE_HIDDEN_AT_PREFIX = "key_vip_nudge_hidden_at_"

private val HIDDEN_FOR = Duration.ofDays(14)

internal class VipNudgeDismissalUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun isHidden(
        userId: TraktId,
        now: Instant,
    ): Boolean {
        val hiddenAt = dataStore.data.first()[hiddenAtKey(userId)] ?: return false
        val elapsed = Duration.between(Instant.ofEpochMilli(hiddenAt), now)
        return !elapsed.isNegative && elapsed < HIDDEN_FOR
    }

    suspend fun hide(
        userId: TraktId,
        now: Instant,
    ) {
        dataStore.edit { prefs ->
            prefs[hiddenAtKey(userId)] = now.toEpochMilli()
        }
    }

    private fun hiddenAtKey(userId: TraktId): Preferences.Key<Long> {
        return longPreferencesKey("$KEY_VIP_NUDGE_HIDDEN_AT_PREFIX${userId.value}")
    }
}
