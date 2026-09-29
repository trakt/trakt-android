package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.CrewPerson.Companion
import tv.trakt.trakt.common.networking.CrewMemberDto

@Immutable
data class CrewPerson(
    val person: Person,
    val jobs: ImmutableList<String> = EmptyImmutableList,
    val episodesCount: Int = 0,
) {
    companion object
}

fun Companion.fromDto(dto: CrewMemberDto): CrewPerson {
    return CrewPerson(
        person = Person.fromDto(dto.person),
        jobs = dto.jobs.toImmutableList(),
        episodesCount = dto.episodeCount ?: 0,
    )
}
