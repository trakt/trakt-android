package tv.trakt.trakt.core.search.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.networking.SearchItemDto
import tv.trakt.trakt.core.search.data.remote.SearchRemoteDataSource

private const val DEFAULT_SEARCH_LIMIT = 50

internal class GetSearchResultsUseCase(
    private val remoteSource: SearchRemoteDataSource,
) {
    suspend fun getSearchResults(query: String): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }

        return coroutineScope {
            val exactResultsAsync = async {
                remoteSource.getShowsMovies(query, DEFAULT_SEARCH_LIMIT, exact = true)
            }
            val normalResultsAsync = async {
                remoteSource.getShowsMovies(query, DEFAULT_SEARCH_LIMIT, exact = false)
            }

            val exactResults = exactResultsAsync.await()
            val normalResults = normalResultsAsync.await()

            (exactResults + normalResults)
                .distinctBy {
                    getDistinctKey(it)
                }
        }
    }

    suspend fun getShowsSearchResults(query: String): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }

        return coroutineScope {
            val exactResultsAsync = async {
                remoteSource.getShows(query, DEFAULT_SEARCH_LIMIT, exact = true)
            }
            val normalResultsAsync = async {
                remoteSource.getShows(query, DEFAULT_SEARCH_LIMIT, exact = false)
            }

            val exactResults = exactResultsAsync.await()
            val normalResults = normalResultsAsync.await()

            (exactResults + normalResults)
                .distinctBy {
                    getDistinctKey(it)
                }
        }
    }

    suspend fun getMoviesSearchResults(query: String): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }

        return coroutineScope {
            val exactResultsAsync = async {
                remoteSource.getMovies(query, DEFAULT_SEARCH_LIMIT, exact = true)
            }
            val normalResultsAsync = async {
                remoteSource.getMovies(query, DEFAULT_SEARCH_LIMIT, exact = false)
            }

            val exactResults = exactResultsAsync.await()
            val normalResults = normalResultsAsync.await()

            (exactResults + normalResults)
                .distinctBy {
                    getDistinctKey(it)
                }
        }
    }

    suspend fun getPeopleSearchResults(query: String): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }
        return remoteSource.getPeople(query, DEFAULT_SEARCH_LIMIT)
    }

    private fun getDistinctKey(dto: SearchItemDto): String {
        val showId = dto.show?.ids?.trakt
        val movieId = dto.movie?.ids?.trakt
        val personId = dto.person?.ids?.trakt

        return "${dto.type.name}-${showId ?: movieId ?: personId}"
    }
}
