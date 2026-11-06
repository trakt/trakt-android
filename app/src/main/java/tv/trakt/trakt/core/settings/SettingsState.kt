package tv.trakt.trakt.core.settings

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.User

@Immutable
internal data class SettingsState(
    val user: User? = null,
)
