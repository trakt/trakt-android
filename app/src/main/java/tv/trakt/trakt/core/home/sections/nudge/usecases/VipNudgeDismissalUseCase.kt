package tv.trakt.trakt.core.home.sections.nudge.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant

private val KEY_VIP_NUDGE_HIDDEN_AT = longPreferencesKey("key_vip_nudge_hidden_at")

private val HIDDEN_FOR = Duration.ofDays(14)

internal class VipNudgeDismissalUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun isHidden(now: Instant): Boolean {
        val hiddenAt = dataStore.data.first()[KEY_VIP_NUDGE_HIDDEN_AT] ?: return false
        val elapsed = Duration.between(Instant.ofEpochMilli(hiddenAt), now)
        return !elapsed.isNegative && elapsed < HIDDEN_FOR
    }

    suspend fun hide(now: Instant) {
        dataStore.edit { prefs ->
            prefs[KEY_VIP_NUDGE_HIDDEN_AT] = now.toEpochMilli()
        }
    }
}
