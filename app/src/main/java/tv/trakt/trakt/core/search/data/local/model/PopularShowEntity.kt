package tv.trakt.trakt.core.search.data.local.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Show
import java.time.Instant

@Serializable
internal data class PopularShowEntity(
    val show: Show,
    val rank: Int,
    val createdAt: String,
) {
    companion object
}

internal fun PopularShowEntity.Companion.create(
    show: Show,
    rank: Int,
    createdAt: Instant,
): PopularShowEntity {
    return PopularShowEntity(
        show = show,
        rank = rank,
        createdAt = createdAt.toString(),
    )
}
