package tv.trakt.trakt.core.summary.episodes.features.actors.usecases

import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource

internal class GetEpisodeDirectorUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getDirector(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ): Person {
        return remoteSource.getCastCrew(
            showId = showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
        ).crew
            ?.get("directing")
            ?.firstOrNull { it.job.equals("director", ignoreCase = true) }
            ?.let { Person.fromDto(it.person) }
            ?.also {
                peopleLocalSource.upsertPeople(listOf(it))
            } ?: Person.Unknown
    }
}
