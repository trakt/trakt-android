package tv.trakt.trakt.common.core.streamings.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingType

/**
 * Every way a single media can be watched in one country, flattened for the details
 * "Where to Watch" row.
 */
@Immutable
data class StreamingsResult(
    val streamings: ImmutableList<Pair<StreamingService, StreamingType>>,
    val ranks: Ranks,
    val justWatchLink: String?,
) {
    @Immutable
    data class Ranks(
        val rank: Int?,
        val delta: Int?,
        val link: String?,
    )
}
