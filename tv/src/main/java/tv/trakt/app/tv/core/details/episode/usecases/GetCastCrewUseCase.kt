package tv.trakt.app.tv.core.details.episode.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.common.model.CastPerson
import tv.trakt.app.tv.common.model.Person
import tv.trakt.app.tv.common.model.SeasonEpisode
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.common.model.fromDto
import tv.trakt.app.tv.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.app.tv.core.people.data.local.PeopleLocalDataSource

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
