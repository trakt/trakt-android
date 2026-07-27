package tv.trakt.trakt.core.search.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import tv.trakt.trakt.common.networking.SearchItemDto
import tv.trakt.trakt.core.search.data.remote.SearchRemoteDataSource
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_SEARCH_LIMIT = 50
private const val DEFAULT_EXACT_SEARCH_LIMIT = 20

/**
 * Score at or above which an exact hit may outrank fuzzy results. The API's confidence
 * bands are whole numbers (2 certain, 1 strong, 0 deep) and this threshold deliberately
 * sits between strong and deep, matching web. Magnitudes are not comparable across
 * search sources, so bucket on the threshold rather than sorting by raw score.
 */
private const val CONFIDENT_EXACT_SCORE = 0.5

internal class GetSearchResultsUseCase(
    private val remoteSource: SearchRemoteDataSource,
) {
    suspend fun getSearchResults(query: String): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }

        return coroutineScope {
            val exactResultsAsync = async {
                exactOrEmpty {
                    remoteSource.getShowsMovies(query, DEFAULT_EXACT_SEARCH_LIMIT, exact = true)
                }
            }
            val normalResultsAsync = async {
                remoteSource.getShowsMovies(query, DEFAULT_SEARCH_LIMIT, exact = false)
            }

            weave(
                exactResults = exactResultsAsync.await(),
                normalResults = normalResultsAsync.await(),
            )
        }
    }

    suspend fun getShowsSearchResults(query: String): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }

        return coroutineScope {
            val exactResultsAsync = async {
                exactOrEmpty {
                    remoteSource.getShows(query, DEFAULT_EXACT_SEARCH_LIMIT, exact = true)
                }
            }
            val normalResultsAsync = async {
                remoteSource.getShows(query, DEFAULT_SEARCH_LIMIT, exact = false)
            }

            weave(
                exactResults = exactResultsAsync.await(),
                normalResults = normalResultsAsync.await(),
            )
        }
    }

    suspend fun getMoviesSearchResults(query: String): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }

        return coroutineScope {
            val exactResultsAsync = async {
                exactOrEmpty {
                    remoteSource.getMovies(query, DEFAULT_EXACT_SEARCH_LIMIT, exact = true)
                }
            }
            val normalResultsAsync = async {
                remoteSource.getMovies(query, DEFAULT_SEARCH_LIMIT, exact = false)
            }

            weave(
                exactResults = exactResultsAsync.await(),
                normalResults = normalResultsAsync.await(),
            )
        }
    }

    suspend fun getPeopleSearchResults(query: String): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }
        return remoteSource.getPeople(query, DEFAULT_SEARCH_LIMIT)
    }

    suspend fun getListsSearchResults(query: String): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }
        return remoteSource.getLists(query, DEFAULT_SEARCH_LIMIT)
    }

    /**
     * Only an unambiguous exact hit leads. A title shared with other entries trails the
     * fuzzy results rather than displacing what the user most likely meant.
     */
    private fun weave(
        exactResults: List<SearchItemDto>,
        normalResults: List<SearchItemDto>,
    ): List<SearchItemDto> {
        val (confident, deep) = exactResults.partition {
            it.score.toDouble() >= CONFIDENT_EXACT_SCORE
        }

        return (confident + normalResults + deep)
            .distinctBy {
                getDistinctKey(it)
            }
    }

    /**
     * The exact pass is an enrichment of the fuzzy one, so its failure degrades the
     * results instead of failing the whole search.
     */
    private suspend fun exactOrEmpty(
        fetch: suspend () -> List<SearchItemDto>,
    ): List<SearchItemDto> = try {
        fetch()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        Timber.w(error, "Exact search failed, falling back to fuzzy results only")
        emptyList()
    }

    private fun getDistinctKey(dto: SearchItemDto): String {
        val showId = dto.show?.ids?.trakt
        val movieId = dto.movie?.ids?.trakt
        val personId = dto.person?.ids?.trakt

        return "${dto.type.name}-${showId ?: movieId ?: personId}"
    }
}
