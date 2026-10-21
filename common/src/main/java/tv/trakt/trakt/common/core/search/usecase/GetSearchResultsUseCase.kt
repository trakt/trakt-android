package tv.trakt.trakt.common.core.search.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import tv.trakt.trakt.common.data.remote.SearchRemoteSource
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.networking.SearchItemDto
import tv.trakt.trakt.common.networking.TrendingSearchDto
import kotlin.coroutines.cancellation.CancellationException
import org.openapitools.client.models.GetSearchQuery200ResponseInner.Type as SearchItemType
import org.openapitools.client.models.GetSearchTrending200ResponseInner.Type as TrendingSearchType

private const val DEFAULT_SEARCH_LIMIT = 50
private const val DEFAULT_EXACT_SEARCH_LIMIT = 20
private const val TRENDING_SHOWS = "shows"
private const val TRENDING_MOVIES = "movies"

/**
 * Score at or above which an exact hit may outrank fuzzy results. The API's confidence
 * bands are whole numbers (2 certain, 1 strong, 0 deep) and this threshold deliberately
 * sits between strong and deep, matching web. Magnitudes are not comparable across
 * search sources, so bucket on the threshold rather than sorting by raw score.
 */
private const val CONFIDENT_EXACT_SCORE = 0.5

class GetSearchResultsUseCase(
    private val remoteSource: SearchRemoteSource,
) {
    suspend fun getSearchResults(query: String): List<SearchItemDto> {
        return searchMedia(
            query = query,
            trendingTypes = listOf(TRENDING_SHOWS, TRENDING_MOVIES),
        ) { limit, exact ->
            remoteSource.getShowsMovies(query, limit, exact)
        }
    }

    suspend fun getShowsSearchResults(query: String): List<SearchItemDto> {
        return searchMedia(
            query = query,
            trendingTypes = listOf(TRENDING_SHOWS),
        ) { limit, exact ->
            remoteSource.getShows(query, limit, exact)
        }
    }

    suspend fun getMoviesSearchResults(query: String): List<SearchItemDto> {
        return searchMedia(
            query = query,
            trendingTypes = listOf(TRENDING_MOVIES),
        ) { limit, exact ->
            remoteSource.getMovies(query, limit, exact)
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

    private suspend fun searchMedia(
        query: String,
        trendingTypes: List<String>,
        fetch: suspend (limit: Int, exact: Boolean) -> List<SearchItemDto>,
    ): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }

        return coroutineScope {
            val exactResultsAsync = async {
                enrichmentOrEmpty { fetch(DEFAULT_EXACT_SEARCH_LIMIT, true) }
            }
            val trendingResultsAsync = async {
                enrichmentOrEmpty { getTrending(query, trendingTypes) }
            }
            val normalResultsAsync = async {
                fetch(DEFAULT_SEARCH_LIMIT, false)
            }

            weave(
                exactResults = exactResultsAsync.await(),
                trendingResults = trendingResultsAsync.await(),
                normalResults = normalResultsAsync.await(),
            )
        }
    }

    private suspend fun getTrending(
        query: String,
        types: List<String>,
    ): List<SearchItemDto> {
        return coroutineScope {
            types
                .map { type -> async { remoteSource.getTrending(query, type) } }
                .flatMap { it.await() }
                .sortedByDescending { it.count }
                .mapNotNull(::toSearchItem)
        }
    }

    /**
     * Only an unambiguous exact hit leads, followed by what others search for with the same
     * query. A title shared with other entries trails the fuzzy results rather than displacing
     * what the user most likely meant.
     */
    private fun weave(
        exactResults: List<SearchItemDto>,
        trendingResults: List<SearchItemDto>,
        normalResults: List<SearchItemDto>,
    ): List<SearchItemDto> {
        val (confident, deep) = exactResults.partition {
            it.score.toDouble() >= CONFIDENT_EXACT_SCORE
        }

        return (confident + trendingResults + normalResults + deep)
            .distinctBy {
                getDistinctKey(it)
            }
    }

    private fun toSearchItem(dto: TrendingSearchDto): SearchItemDto? {
        val type = when (dto.type) {
            TrendingSearchType.MOVIE -> SearchItemType.MOVIE
            TrendingSearchType.SHOW -> SearchItemType.SHOW
            TrendingSearchType.PERSON -> return null
        }

        val traktId = dto.show?.ids?.trakt ?: dto.movie?.ids?.trakt
        if (traktId == null || traktId == 0) return null

        return SearchItemDto(
            score = dto.count.toLong(),
            type = type,
            movie = dto.movie,
            show = dto.show,
        )
    }

    /**
     * The exact and trending passes enrich the fuzzy one, so their failure degrades the
     * results instead of failing the whole search.
     */
    private suspend fun enrichmentOrEmpty(fetch: suspend () -> List<SearchItemDto>): List<SearchItemDto> {
        return try {
            fetch()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Timber.recordError(error)
            emptyList()
        }
    }

    private fun getDistinctKey(dto: SearchItemDto): String {
        val showId = dto.show?.ids?.trakt
        val movieId = dto.movie?.ids?.trakt
        val personId = dto.person?.ids?.trakt

        return "${dto.type.name}-${showId ?: movieId ?: personId}"
    }
}
