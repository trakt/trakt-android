package tv.trakt.trakt.core.settings.features.younify

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class YounifyState(
    val user: User? = null,
    val younifyDetails: User? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
