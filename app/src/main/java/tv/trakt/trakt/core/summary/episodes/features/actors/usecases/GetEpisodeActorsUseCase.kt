package tv.trakt.trakt.core.summary.episodes.features.actors.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource

internal class GetEpisodeActorsUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
//    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getCastCrew(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): ImmutableList<CastPerson> {
        val castCrew = remoteSource.getEpisodeCastCrew(
            showId = showId,
            season = season,
            episode = episode,
        )

        val cast = (castCrew.cast ?: emptyList())
            .distinctBy { it.person.ids.trakt }
            .take(30)
            .map { person ->
                CastPerson(
                    characters = person.characters,
                    person = Person.fromDto(person.person),
                )
            }

//        peopleLocalSource.upsertPeople(cast.map { it.person })

        return cast.toImmutableList()
    }
}