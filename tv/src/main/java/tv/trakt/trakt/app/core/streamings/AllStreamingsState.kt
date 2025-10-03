package tv.trakt.trakt.app.core.streamings

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.app.core.streamings.model.StreamingServiceRow
import tv.trakt.trakt.common.model.streamings.StreamingType

@Immutable
internal data class AllStreamingsState(
    val services: ImmutableMap<StreamingType, List<StreamingServiceRow>>? = null,
    val loading: Boolean = true,
    val error: Exception? = null,
)
