package tv.trakt.app.tv.core.profile

import androidx.compose.runtime.Immutable
import tv.trakt.app.tv.common.model.User

@Immutable
internal data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val backgroundUrl: String? = null,
    val error: Exception? = null,
)
