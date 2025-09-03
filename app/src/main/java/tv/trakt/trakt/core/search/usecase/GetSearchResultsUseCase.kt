package tv.trakt.trakt.core.search.usecase

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
        return remoteSource.getShowsMovies(query, DEFAULT_SEARCH_LIMIT)
    }

    suspend fun getShowsSearchResults(query: String): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }
        return remoteSource.getShows(query, DEFAULT_SEARCH_LIMIT)
    }

    suspend fun getMoviesSearchResults(query: String): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }
        return remoteSource.getMovies(query, DEFAULT_SEARCH_LIMIT)
    }
}
