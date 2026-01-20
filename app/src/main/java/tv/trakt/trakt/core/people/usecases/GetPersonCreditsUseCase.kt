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
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem

internal class GetPersonCreditsUseCase(
    private val peopleRemoteSource: PeopleRemoteDataSource,
) {
    private val mutex = Mutex()

    suspend fun getShowCredits(personId: TraktId): ImmutableMap<String, ImmutableList<PersonCreditItem.ShowItem>> {
        val response = peopleRemoteSource.getPersonShowsCredits(personId)
        val results = mutableMapOf<String, ImmutableList<PersonCreditItem.ShowItem>>()

        val actingResults = mutableListOf<PersonCreditItem.ShowItem>()
        val selfResults = mutableListOf<PersonCreditItem.ShowItem>()
        val narratorResults = mutableListOf<PersonCreditItem.ShowItem>()

        response.cast?.asyncMap {
            mutex.withLock {
                val show = PersonCreditItem.ShowItem(
                    show = Show.fromDto(it.show),
                    credit = it.character,
                )

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
                    .map {
                        PersonCreditItem.ShowItem(
                            show = Show.fromDto(it.show),
                            credit = entry.value.firstOrNull()?.job ?: entry.key,
                        )
                    }
                    .sortedByDescending { it.released }
                    .toImmutableList()
            }?.let {
                results.putAll(it)
            }

        return results.toImmutableMap()
    }

    suspend fun getMovieCredits(personId: TraktId): ImmutableMap<String, ImmutableList<PersonCreditItem.MovieItem>> {
        val response = peopleRemoteSource.getPersonMoviesCredits(personId)
        val results = mutableMapOf<String, ImmutableList<PersonCreditItem.MovieItem>>()

        val actingResults = mutableListOf<PersonCreditItem.MovieItem>()
        val selfResults = mutableListOf<PersonCreditItem.MovieItem>()
        val narratorResults = mutableListOf<PersonCreditItem.MovieItem>()

        response.cast?.asyncMap {
            mutex.withLock {
                val movie = PersonCreditItem.MovieItem(
                    movie = Movie.fromDto(it.movie),
                    credit = it.character,
                )

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
                    .map {
                        PersonCreditItem.MovieItem(
                            movie = Movie.fromDto(it.movie),
                            credit = entry.value.firstOrNull()?.job ?: entry.key,
                        )
                    }
                    .sortedByDescending { it.released }
                    .toImmutableList()
            }?.let {
                results.putAll(it)
            }

        return results.toImmutableMap()
    }
}
