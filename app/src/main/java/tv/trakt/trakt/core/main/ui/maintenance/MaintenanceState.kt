package tv.trakt.trakt.core.main.ui.maintenance

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.core.main.model.MaintenanceMode

@Immutable
internal data class MaintenanceState(
    val maintenance: MaintenanceMode? = null,
)
