package tv.trakt.trakt.app.core.details.episode.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.model.CastPerson
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.app.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

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
