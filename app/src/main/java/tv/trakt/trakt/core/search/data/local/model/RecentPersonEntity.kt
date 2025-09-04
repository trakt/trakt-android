package tv.trakt.trakt.core.search.data.local.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Person
import java.time.Instant

@Serializable
internal data class RecentPersonEntity(
    val person: Person,
    val createdAt: String, // Format: "2022-12-03T10:15:30Z"
) {
    companion object
}

internal fun RecentPersonEntity.Companion.create(
    person: Person,
    createdAt: Instant,
): RecentPersonEntity {
    return RecentPersonEntity(
        person = person,
        createdAt = createdAt.toString(),
    )
}
