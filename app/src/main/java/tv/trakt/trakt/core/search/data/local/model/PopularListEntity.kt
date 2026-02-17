package tv.trakt.trakt.core.search.data.local.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.CustomList
import java.time.Instant

@Serializable
internal data class PopularListEntity(
    val list: CustomList,
    val rank: Int,
    val createdAt: String,
) {
    companion object
}

internal fun PopularListEntity.Companion.create(
    list: CustomList,
    rank: Int,
    createdAt: Instant,
): PopularListEntity {
    return PopularListEntity(
        list = list,
        rank = rank,
        createdAt = createdAt.toString(),
    )
}
