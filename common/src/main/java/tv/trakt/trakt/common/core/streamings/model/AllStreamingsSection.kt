package tv.trakt.trakt.common.core.streamings.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.streamings.StreamingType

/**
 * Streaming sources grouped by the way they are watched (favorite, subscription, purchase, …).
 */
@Immutable
data class AllStreamingsSection(
    val type: StreamingType,
    val rows: ImmutableList<StreamingServiceRow>,
)
