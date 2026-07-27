package tv.trakt.trakt.core.summary.shows.features.seasons.all.usecases

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource

internal class GetSeasonPeopleUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getCastCrew(
        showId: TraktId,
        season: Int,
    ): Result {
        val castCrew = remoteSource.getCastCrew(
            showId,
            season,
        )

        val cast = (castCrew.cast ?: emptyList())
            .distinctBy { it.person.ids.trakt }
            .map { person ->
                CastPerson(
                    characters = person.characters,
                    person = Person.fromDto(person.person),
                    episodesCount = person.episodeCount ?: 0,
                )
            }
            .toImmutableList()

        val crew = (castCrew.crew ?: emptyMap())
            .values
            .flatten()
            .groupBy { it.person.ids.trakt }
            .map { (_, roles) ->
                CrewPerson(
                    person = Person.fromDto(roles.first().person),
                    jobs = roles
                        .flatMap { role -> role.jobs.map { job -> job to (role.episodeCount ?: 0) } }
                        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
                        .mapValues { (_, episodeCounts) -> episodeCounts.max() }
                        .entries
                        .sortedByDescending { it.value }
                        .map { it.key }
                        .toImmutableList(),
                    episodesCount = roles.maxOf { it.episodeCount ?: 0 },
                )
            }
            .sortedByDescending { it.episodesCount }
            .toImmutableList()

        peopleLocalSource.upsertPeople(cast.map { it.person } + crew.map { it.person })

        return Result(
            cast = cast,
            crew = crew,
        )
    }

    @Immutable
    data class Result(
        val cast: ImmutableList<CastPerson> = EmptyImmutableList,
        val crew: ImmutableList<CrewPerson> = EmptyImmutableList,
    )
}
