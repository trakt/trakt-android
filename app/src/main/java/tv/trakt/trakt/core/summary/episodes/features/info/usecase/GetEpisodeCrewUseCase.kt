package tv.trakt.trakt.core.summary.episodes.features.info.usecase

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.remote.EpisodesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetEpisodeCrewUseCase(
    private val remoteSource: EpisodesRemoteSource,
    private val peopleLocalSource: PeopleLocalSource,
) {
    suspend fun getCrew(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): Result {
        return remoteSource.getCastCrew(showId, season, episode).crew?.let { crew ->
            val directors = crew["directing"]
                ?.filter { it.job.equals("director", ignoreCase = true) }
                ?.map { CrewPerson.fromDto(it) }
                ?: EmptyImmutableList

            val writers = crew["writing"]
                ?.map { CrewPerson.fromDto(it) }
                ?: EmptyImmutableList

            peopleLocalSource.upsertPeople((directors + writers).map { it.person })

            Result(
                directors = directors.toImmutableList(),
                writers = writers.toImmutableList(),
            )
        } ?: Result()
    }

    @Immutable
    data class Result(
        val directors: ImmutableList<CrewPerson> = EmptyImmutableList,
        val writers: ImmutableList<CrewPerson> = EmptyImmutableList,
    )
}
