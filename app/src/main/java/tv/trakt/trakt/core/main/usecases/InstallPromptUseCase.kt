package tv.trakt.trakt.core.main.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit.DAYS

private val KEY_FIRST_LAUNCH_AT = longPreferencesKey("key_first_launch_at")
private val KEY_INSTALL_PROMPTED_AT = longPreferencesKey("key_install_prompted_at")

private const val MIN_USAGE_DAYS = 1L
private const val REPROMPT_MONTHS = 3L

internal class InstallPromptUseCase(
    private val mainDataStore: DataStore<Preferences>,
) {
    suspend fun shouldPromptInstall(): Boolean {
        val prefs = mainDataStore.data.first()
        val promptedAt = prefs[KEY_INSTALL_PROMPTED_AT]
        if (promptedAt != null) {
            return Instant.ofEpochMilli(promptedAt)
                .atZone(ZoneOffset.UTC)
                .plusMonths(REPROMPT_MONTHS)
                .toInstant()
                .isBefore(nowUtcInstant())
        }

        val firstLaunchAt = prefs[KEY_FIRST_LAUNCH_AT]

        if (firstLaunchAt == null) {
            mainDataStore.edit {
                it[KEY_FIRST_LAUNCH_AT] = nowUtcInstant().toEpochMilli()
            }
            return false
        }

        return Instant.ofEpochMilli(firstLaunchAt)
            .plus(MIN_USAGE_DAYS, DAYS)
            .isBefore(nowUtcInstant())
    }

    suspend fun markInstallPrompted() {
        mainDataStore.edit {
            it[KEY_INSTALL_PROMPTED_AT] = nowUtcInstant().toEpochMilli()
        }
    }
}
