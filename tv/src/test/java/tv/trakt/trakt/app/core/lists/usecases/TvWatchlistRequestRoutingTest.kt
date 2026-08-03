package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import tv.trakt.trakt.app.core.lists.filters.TvListFilterConfiguration
import tv.trakt.trakt.app.core.lists.filters.TvListRequest
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistRemoteDataSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.WatchlistItemDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import tv.trakt.trakt.common.networking.WatchlistShowDto

class TvWatchlistRequestRoutingTest {
    @Test
    fun `movie watchlist forwards paging filters and sorting`() =
        runBlocking {
            val remote = FakeWatchlistRemoteDataSource()
            val useCase = GetListsMoviesWatchlistUseCase(
                remoteSource = remote,
                localMovieSource = FakeMovieLocalDataSource(),
            )
            val filter = TvListFilterConfiguration.MoviesWatchlist.defaultFilter.copy(
                years = 1990 to 1999,
                hideWatched = true,
            )
            val sorting = Sorting(SortType.Rating, SortOrder.Desc)

            val page = useCase.getMovies(
                TvListRequest(
                    page = 3,
                    limit = 25,
                    filter = filter,
                    sorting = sorting,
                ),
            )

            assertEquals("movies", remote.endpoint)
            assertEquals(3, remote.page)
            assertEquals(25, remote.limit)
            assertEquals(filter, remote.filter)
            assertEquals(sorting, remote.sorting)
            assertEquals(4, page.nextPage)
            assertFalse(page.hasMore)
        }

    @Test
    fun `show watchlist forwards paging filters and sorting`() =
        runBlocking {
            val remote = FakeWatchlistRemoteDataSource()
            val useCase = GetListsShowsWatchlistUseCase(
                remoteSource = remote,
                localShowSource = FakeShowLocalDataSource(),
            )
            val filter = TvListFilterConfiguration.ShowsWatchlist.defaultFilter.copy(
                rating = 60 to 100,
            )
            val sorting = Sorting(SortType.Title, SortOrder.Asc)

            val page = useCase.getShows(
                TvListRequest(
                    page = 2,
                    limit = 30,
                    filter = filter,
                    sorting = sorting,
                ),
            )

            assertEquals("shows", remote.endpoint)
            assertEquals(2, remote.page)
            assertEquals(30, remote.limit)
            assertEquals(filter, remote.filter)
            assertEquals(sorting, remote.sorting)
            assertEquals(3, page.nextPage)
            assertFalse(page.hasMore)
        }
}

private class FakeWatchlistRemoteDataSource : UserWatchlistRemoteDataSource {
    var endpoint: String? = null
    var page: Int? = null
    var limit: Int? = null
    var filter: GlobalFilter? = null
    var sorting: Sorting? = null

    override suspend fun getWatchlistMinimal(): Pair<Set<TraktId>, Set<TraktId>> {
        return emptySet<TraktId>() to emptySet()
    }

    override suspend fun getWatchlist(
        page: Int?,
        limit: Int?,
        extended: String?,
        sorting: Sorting?,
        filters: GlobalFilter?,
    ): List<WatchlistItemDto> {
        error("Mixed watchlist endpoint is not expected")
    }

    override suspend fun getWatchlistShows(
        page: Int?,
        limit: Int?,
        extended: String?,
        sorting: Sorting?,
        hide: String?,
        filters: GlobalFilter?,
    ): List<WatchlistShowDto> {
        capture(
            endpoint = "shows",
            page = page,
            limit = limit,
            sorting = sorting,
            filter = filters,
        )
        return emptyList()
    }

    override suspend fun getWatchlistMovies(
        page: Int?,
        limit: Int?,
        extended: String?,
        sorting: Sorting?,
        hide: String?,
        filters: GlobalFilter?,
    ): List<WatchlistMovieDto> {
        capture(
            endpoint = "movies",
            page = page,
            limit = limit,
            sorting = sorting,
            filter = filters,
        )
        return emptyList()
    }

    private fun capture(
        endpoint: String,
        page: Int?,
        limit: Int?,
        sorting: Sorting?,
        filter: GlobalFilter?,
    ) {
        this.endpoint = endpoint
        this.page = page
        this.limit = limit
        this.sorting = sorting
        this.filter = filter
    }
}

private class FakeMovieLocalDataSource : MovieLocalDataSource {
    override suspend fun getMovie(movieId: TraktId): Movie? = null

    override suspend fun upsertMovies(movies: List<Movie>) = Unit
}

private class FakeShowLocalDataSource : ShowLocalDataSource {
    override suspend fun getShow(showId: TraktId): Show? = null

    override suspend fun upsertShows(shows: List<Show>) = Unit
}
