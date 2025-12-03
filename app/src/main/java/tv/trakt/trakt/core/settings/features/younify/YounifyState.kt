package tv.trakt.trakt.core.settings.features.younify

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.younify.sdk.connect.StreamingService

@Immutable
internal data class YounifyState(
    val user: User? = null,
    val younifyServices: ImmutableList<StreamingService>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
