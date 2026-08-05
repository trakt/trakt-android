package tv.trakt.trakt.common.core.streamings.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.streamings.StreamingService

/**
 * A single streaming source (Netflix, Apple TV, …) with every country it is available in.
 */
@Immutable
data class StreamingServiceRow(
    val source: String,
    val services: ImmutableList<StreamingService>,
)
