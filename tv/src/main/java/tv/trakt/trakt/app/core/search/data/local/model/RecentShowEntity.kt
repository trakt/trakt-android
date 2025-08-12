package tv.trakt.trakt.app.core.search.data.local.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Show
import java.time.Instant

@Serializable
internal data class RecentShowEntity(
    val show: Show,
    val createdAt: String, // Format: "2022-12-03T10:15:30Z"
) {
    companion object
}

internal fun RecentShowEntity.Companion.create(
    show: Show,
    createdAt: Instant,
): RecentShowEntity {
    return RecentShowEntity(
        show = show,
        createdAt = createdAt.toString(),
    )
}
