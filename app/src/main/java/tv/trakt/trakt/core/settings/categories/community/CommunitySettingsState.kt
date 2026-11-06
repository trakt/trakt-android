package tv.trakt.trakt.core.settings.categories.community

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.User

@Immutable
internal data class CommunitySettingsState(
    val user: User? = null,
)
