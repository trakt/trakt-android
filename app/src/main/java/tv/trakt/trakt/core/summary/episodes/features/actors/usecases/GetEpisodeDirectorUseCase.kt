package tv.trakt.trakt.core.summary.episodes.features.actors.usecases

import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.remote.EpisodesRemoteSource
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetEpisodeDirectorUseCase(
    private val remoteSource: EpisodesRemoteSource,
    private val peopleLocalSource: PeopleLocalSource,
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
