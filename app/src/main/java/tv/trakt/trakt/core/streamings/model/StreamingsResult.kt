package tv.trakt.trakt.core.streamings.model

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingType

data class StreamingsResult(
    val streamings: ImmutableList<Pair<StreamingService, StreamingType>>,
    val ranks: Ranks,
    val justWatchLink: String?,
) {
    data class Ranks(
        val rank: Int?,
        val delta: Int?,
        val link: String?,
    )
}
