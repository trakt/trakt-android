package tv.trakt.trakt.core.people.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.people.data.remote.PeopleRemoteDataSource

internal class GetPersonCreditsUseCase(
    private val peopleRemoteSource: PeopleRemoteDataSource,
) {
    suspend fun getShowCredits(personId: TraktId): ImmutableMap<String, ImmutableList<Show>> {
        val response = peopleRemoteSource.getPersonShowsCredits(personId)
        val results = mutableMapOf<String, ImmutableList<Show>>()

        val actingResults = response.cast
            ?.map { Show.fromDto(it.show) }
            ?.sortedByDescending { it.rating.votes }
            ?.toImmutableList()

        val crewResults = response.crew
            ?.mapValues { entry ->
                entry.value
                    .map { Show.fromDto(it.show) }
                    .sortedByDescending { it.rating.votes }
                    .toImmutableList()
            }

        results["acting"] = actingResults ?: EmptyImmutableList
        if (crewResults != null) {
            results.putAll(crewResults)
        }

        return results.toImmutableMap()
    }

    suspend fun getMovieCredits(personId: TraktId): ImmutableMap<String, ImmutableList<Movie>> {
        val response = peopleRemoteSource.getPersonMoviesCredits(personId)
        val results = mutableMapOf<String, ImmutableList<Movie>>()

        val actingResults = response.cast
            ?.map { Movie.fromDto(it.movie) }
            ?.sortedByDescending { it.rating.votes }
            ?.toImmutableList()

        val crewResults = response.crew
            ?.mapValues { entry ->
                entry.value
                    .map { Movie.fromDto(it.movie) }
                    .sortedByDescending { it.rating.votes }
                    .toImmutableList()
            }

        results["acting"] = actingResults ?: EmptyImmutableList
        if (crewResults != null) {
            results.putAll(crewResults)
        }

        return results.toImmutableMap()
    }
}
