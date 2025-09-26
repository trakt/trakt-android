package tv.trakt.trakt.core.main

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.User

@Immutable
internal data class MainState(
    val user: User? = null,
)
