package tv.trakt.trakt.core.main.usecases

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_MAINTENANCE_ENABLED
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_MAINTENANCE_MESSAGE
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_MAINTENANCE_TITLE
import tv.trakt.trakt.common.firebase.config.RemoteConfigManager
import tv.trakt.trakt.core.main.model.MaintenanceMode

private val maintenanceKeys = setOf(
    MOBILE_MAINTENANCE_ENABLED,
    MOBILE_MAINTENANCE_TITLE,
    MOBILE_MAINTENANCE_MESSAGE,
)

internal class MaintenanceModeUseCase(
    private val remoteConfigManager: RemoteConfigManager,
) {
    fun observe(): Flow<MaintenanceMode?> {
        return remoteConfigManager.observe(maintenanceKeys) {
            maintenanceMode()
        }
    }
}

private fun FirebaseRemoteConfig.maintenanceMode(): MaintenanceMode? {
    if (!getBoolean(MOBILE_MAINTENANCE_ENABLED)) {
        return null
    }
    return MaintenanceMode(
        title = getString(MOBILE_MAINTENANCE_TITLE).ifBlank { null },
        message = getString(MOBILE_MAINTENANCE_MESSAGE).ifBlank { null },
    )
}
