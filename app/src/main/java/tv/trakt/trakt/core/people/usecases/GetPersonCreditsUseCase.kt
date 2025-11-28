package tv.trakt.trakt.core.people.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.people.data.remote.PeopleRemoteDataSource

internal class GetPersonCreditsUseCase(
    private val peopleRemoteSource: PeopleRemoteDataSource,
) {
    private val mutex = Mutex()

    suspend fun getShowCredits(personId: TraktId): ImmutableMap<String, ImmutableList<Show>> {
        val response = peopleRemoteSource.getPersonShowsCredits(personId)
        val results = mutableMapOf<String, ImmutableList<Show>>()

        val actingResults = mutableListOf<Show>()
        val selfResults = mutableListOf<Show>()
        val narratorResults = mutableListOf<Show>()

        response.cast?.asyncMap {
            mutex.withLock {
                val show = Show.fromDto(it.show)

                val isSelf = it.character.contains("self", ignoreCase = true)
                val isNarrator = it.character.contains("narrator", ignoreCase = true)

                when {
                    isNarrator -> narratorResults.add(show)
                    isSelf -> selfResults.add(show)
                    !isSelf -> actingResults.add(show)
                }
            }
        }

        if (actingResults.isNotEmpty()) {
            results["acting"] = actingResults
                .sortedByDescending { it.released }
                .toImmutableList()
        }

        if (selfResults.isNotEmpty()) {
            results["self"] = selfResults
                .sortedByDescending { it.released }
                .toImmutableList()
        }

        if (narratorResults.isNotEmpty()) {
            results["narrator"] = narratorResults
                .sortedByDescending { it.released }
                .toImmutableList()
        }

        response.crew
            ?.mapValues { entry ->
                entry.value
                    .map { Show.fromDto(it.show) }
                    .sortedByDescending { it.released }
                    .toImmutableList()
            }?.let {
                results.putAll(it)
            }

        return results.toImmutableMap()
    }

    suspend fun getMovieCredits(personId: TraktId): ImmutableMap<String, ImmutableList<Movie>> {
        val response = peopleRemoteSource.getPersonMoviesCredits(personId)
        val results = mutableMapOf<String, ImmutableList<Movie>>()

        val actingResults = mutableListOf<Movie>()
        val selfResults = mutableListOf<Movie>()
        val narratorResults = mutableListOf<Movie>()

        response.cast?.asyncMap {
            mutex.withLock {
                val movie = Movie.fromDto(it.movie)

                val isSelf = it.character.contains("self", ignoreCase = true)
                val isNarrator = it.character.contains("narrator", ignoreCase = true)

                when {
                    isNarrator -> narratorResults.add(movie)
                    isSelf -> selfResults.add(movie)
                    !isSelf -> actingResults.add(movie)
                }
            }
        }

        if (actingResults.isNotEmpty()) {
            results["acting"] = actingResults
                .sortedByDescending { it.released }
                .toImmutableList()
        }

        if (selfResults.isNotEmpty()) {
            results["self"] = selfResults
                .sortedByDescending { it.released }
                .toImmutableList()
        }

        if (narratorResults.isNotEmpty()) {
            results["narrator"] = narratorResults
                .sortedByDescending { it.released }
                .toImmutableList()
        }

        response.crew
            ?.mapValues { entry ->
                entry.value
                    .map { Movie.fromDto(it.movie) }
                    .sortedByDescending { it.released }
                    .toImmutableList()
            }?.let {
                results.putAll(it)
            }

        return results.toImmutableMap()
    }
}
