package tv.trakt.trakt.tv.core.main

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.User

@Immutable
internal data class MainState(
    val profile: User? = null,
    val splash: Boolean? = null,
    val isSignedOut: Boolean? = null,
)
