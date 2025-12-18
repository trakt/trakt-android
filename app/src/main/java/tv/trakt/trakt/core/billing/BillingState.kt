package tv.trakt.trakt.core.billing

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class BillingState(
    val user: User? = null,
    val loading: LoadingState = LoadingState.IDLE,
)
