package tv.trakt.trakt.common.data.local.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Person
import java.time.Instant

@Serializable
data class PersonEntity(
    val person: Person,
    val createdAt: String, // Format: "2022-12-03T10:15:30Z"
) {
    companion object Companion
}

fun PersonEntity.Companion.create(
    person: Person,
    createdAt: Instant,
): PersonEntity {
    return PersonEntity(
        person = person,
        createdAt = createdAt.toString(),
    )
}
