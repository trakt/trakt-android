package tv.trakt.trakt.common.data.local.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.lists.CustomList
import java.time.Instant

@Serializable
data class PopularListEntity(
    val list: CustomList,
    val createdAt: String,
) {
    companion object
}

fun PopularListEntity.Companion.create(
    list: CustomList,
    createdAt: Instant,
): PopularListEntity {
    return PopularListEntity(
        list = list,
        createdAt = createdAt.toString(),
    )
}
