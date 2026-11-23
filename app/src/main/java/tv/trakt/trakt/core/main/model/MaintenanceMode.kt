package tv.trakt.trakt.core.main.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class MaintenanceMode(
    val title: String?,
    val message: String?,
)
