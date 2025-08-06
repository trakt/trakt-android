package tv.trakt.trakt.app.core.streamings

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.app.common.model.StreamingService
import tv.trakt.trakt.app.core.streamings.model.StreamingType

@Immutable
internal data class AllStreamingsState(
    val services: ImmutableMap<StreamingType, List<StreamingService>>? = null,
    val loading: Boolean = true,
    val error: Exception? = null,
)
