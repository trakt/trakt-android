package tv.trakt.trakt.tv.core.details.episode.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.common.model.CastPerson
import tv.trakt.trakt.tv.common.model.Person
import tv.trakt.trakt.tv.common.model.SeasonEpisode
import tv.trakt.trakt.tv.common.model.fromDto
import tv.trakt.trakt.tv.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.tv.core.people.data.local.PeopleLocalDataSource

internal class GetCastCrewUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getCastCrew(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ): ImmutableList<CastPerson> {
        val castCrew = remoteSource.getEpisodeCastCrew(
            showId = showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
        )

        val cast = (castCrew.cast ?: emptyList())
            .take(30)
            .map { person ->
                CastPerson(
                    characters = person.characters,
                    person = Person.fromDto(person.person),
                )
            }

        peopleLocalSource.upsertPeople(cast.map { it.person })

        return cast.toImmutableList()
    }
}
