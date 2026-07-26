package tv.trakt.trakt.app.core.details.lists.details.media.usecases

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import tv.trakt.trakt.app.core.lists.filters.TvListFilterConfiguration
import tv.trakt.trakt.app.core.lists.filters.TvListRequest
import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.ListMediaItemDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto

class GetListItemsUseCaseTest {
    @Test
    fun `custom list routes media modes and forwards list options`() =
        runBlocking {
            val remote = FakeListsRemoteDataSource()
            val useCase = GetListItemsUseCase(
                remoteSource = remote,
                showLocalSource = FakeShowLocalDataSource(),
                movieLocalSource = FakeMovieLocalDataSource(),
            )
            val sorting = Sorting(SortType.UserRating, SortOrder.Desc)

            listOf(
                MediaMode.Media to "media",
                MediaMode.Shows to "shows",
                MediaMode.Movies to "movies",
            ).forEach { (mode, expectedEndpoint) ->
                val filter = TvListFilterConfiguration.MixedList.defaultFilter.copy(
                    mode = mode,
                    years = 2000 to 2009,
                )
                val result = useCase.getListItems(
                    listId = TraktId(12),
                    request = TvListRequest(
                        page = 5,
                        limit = 50,
                        filter = filter,
                        sorting = sorting,
                    ),
                )

                assertEquals(expectedEndpoint, remote.endpoint)
                assertEquals(TraktId(12), remote.listId)
                assertEquals(Pagination(5, 50), remote.pagination)
                assertEquals(filter, remote.filter)
                assertEquals(sorting, remote.sorting)
                assertEquals(6, result.nextPage)
                assertFalse(result.hasMore)
            }
        }
}

private class FakeListsRemoteDataSource : ListsRemoteDataSource {
    var endpoint: String? = null
    var listId: TraktId? = null
    var pagination: Pagination? = null
    var sorting: Sorting? = null
    var filter: GlobalFilter? = null

    override suspend fun createList(
        name: String,
        description: String?,
        privacy: String,
    ) = Unit

    override suspend fun editList(
        listId: TraktId,
        name: String,
        description: String?,
        privacy: String,
    ) = Unit

    override suspend fun deleteList(listId: TraktId) = Unit

    override suspend fun addShowToList(
        userId: TraktId,
        listId: TraktId,
        showId: TraktId,
    ) = Unit

    override suspend fun removeShowFromList(
        userId: TraktId,
        listId: TraktId,
        showId: TraktId,
    ) = Unit

    override suspend fun addMovieToList(
        userId: TraktId,
        listId: TraktId,
        movieId: TraktId,
    ) = Unit

    override suspend fun removeMovieFromList(
        userId: TraktId,
        listId: TraktId,
        movieId: TraktId,
    ) = Unit

    override suspend fun getAllListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListItemDto> {
        error("All-items endpoint is not expected by the TV media-only presentation")
    }

    override suspend fun getMediaListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListMediaItemDto> {
        capture("media", listId, sorting, pagination, filters)
        return emptyList()
    }

    override suspend fun getShowListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListShowItemDto> {
        capture("shows", listId, sorting, pagination, filters)
        return emptyList()
    }

    override suspend fun getMovieListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListMovieItemDto> {
        capture("movies", listId, sorting, pagination, filters)
        return emptyList()
    }

    override suspend fun addLikedList(listId: TraktId) = Unit

    override suspend fun removeLikedList(listId: TraktId) = Unit

    override suspend fun reorderListItems(
        listId: TraktId,
        itemsIds: List<Int>,
    ) = Unit

    private fun capture(
        endpoint: String,
        listId: TraktId,
        sorting: Sorting,
        pagination: Pagination,
        filter: GlobalFilter?,
    ) {
        this.endpoint = endpoint
        this.listId = listId
        this.sorting = sorting
        this.pagination = pagination
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
