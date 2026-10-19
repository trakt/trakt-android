package tv.trakt.trakt.common.data.local.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Show
import java.time.Instant

@Serializable
data class PopularShowEntity(
    val show: Show,
    val rank: Int,
    val createdAt: String,
) {
    companion object
}

fun PopularShowEntity.Companion.create(
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
