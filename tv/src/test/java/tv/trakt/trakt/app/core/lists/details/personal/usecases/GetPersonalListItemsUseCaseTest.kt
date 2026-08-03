package tv.trakt.trakt.app.core.lists.details.personal.usecases

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.openapitools.client.models.GetUsersListsListItemsAll200ResponseInner
import tv.trakt.trakt.app.core.lists.filters.TvListFilterConfiguration
import tv.trakt.trakt.app.core.lists.filters.TvListRequest
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsRemoteDataSource
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalList

class GetPersonalListItemsUseCaseTest {
    @Test
    fun `media mode selects matching endpoint and forwards request`() =
        runBlocking {
            val remote = FakePersonalListsRemoteDataSource()
            val useCase = createUseCase(remote)
            val sorting = Sorting(SortType.Released, SortOrder.Desc)

            listOf(
                MediaMode.Media to "media",
                MediaMode.Shows to "shows",
                MediaMode.Movies to "movies",
            ).forEach { (mode, expectedEndpoint) ->
                val filter = TvListFilterConfiguration.MixedList.defaultFilter.copy(
                    mode = mode,
                    rating = 55 to 95,
                )
                useCase.getListItems(
                    listId = TraktId(42),
                    request = TvListRequest(
                        page = 4,
                        limit = 25,
                        filter = filter,
                        sorting = sorting,
                    ),
                )

                assertEquals(expectedEndpoint, remote.endpointCalls.last())
                assertEquals(TraktId(42), remote.listId)
                assertEquals(4, remote.page)
                assertEquals(25, remote.limit)
                assertEquals(filter, remote.filter)
                assertEquals(sorting, remote.sorting)
            }
        }

    @Test
    fun `mixed mode pages past unsupported season and episode rows`() =
        runBlocking {
            val remote = FakePersonalListsRemoteDataSource(
                mediaResponses = mapOf(
                    1 to listOf(
                        unsupportedItem(
                            id = 1,
                            type = GetUsersListsListItemsAll200ResponseInner.Type.SEASON,
                        ),
                        unsupportedItem(
                            id = 2,
                            type = GetUsersListsListItemsAll200ResponseInner.Type.EPISODE,
                        ),
                    ),
                    2 to emptyList(),
                ),
            )
            val useCase = createUseCase(remote)

            val page = useCase.getListItems(
                listId = TraktId(7),
                request = TvListRequest(
                    page = 1,
                    limit = 2,
                    filter = TvListFilterConfiguration.MixedList.defaultFilter,
                    sorting = Sorting.Default,
                ),
            )

            assertEquals(listOf("media", "media"), remote.endpointCalls)
            assertEquals(listOf(1, 2), remote.mediaPageCalls)
            assertEquals(3, page.nextPage)
            assertFalse(page.hasMore)
            assertEquals(0, page.items.size)
        }

    private fun createUseCase(remote: UserPersonalListsRemoteDataSource): GetPersonalListItemsUseCase {
        return GetPersonalListItemsUseCase(
            remoteSource = remote,
            localShowSource = FakeShowLocalDataSource(),
            localMovieSource = FakeMovieLocalDataSource(),
        )
    }
}

private fun unsupportedItem(
    id: Int,
    type: GetUsersListsListItemsAll200ResponseInner.Type,
): ListItemDto {
    return GetUsersListsListItemsAll200ResponseInner(
        rank = id,
        id = id,
        listedAt = "2026-01-01T00:00:00.000Z",
        type = type,
    )
}

private class FakePersonalListsRemoteDataSource(
    private val mediaResponses: Map<Int, List<ListItemDto>> = emptyMap(),
) : UserPersonalListsRemoteDataSource {
    val endpointCalls = mutableListOf<String>()
    val mediaPageCalls = mutableListOf<Int>()
    var listId: TraktId? = null
    var page: Int? = null
    var limit: Int? = null
    var sorting: Sorting? = null
    var filter: GlobalFilter? = null

    override suspend fun getPersonalLists(
        pagination: Pagination,
        userId: String,
    ): List<ListDto> = emptyList()

    override suspend fun getPersonalListsMinimal(): List<V3MinimalList> = emptyList()

    override suspend fun getPersonalListItems(
        listId: TraktId,
        limit: Int,
        page: Int,
        extended: String,
        sorting: Sorting,
        filters: GlobalFilter?,
    ): List<ListItemDto> {
        capture(
            endpoint = "media",
            listId = listId,
            page = page,
            limit = limit,
            sorting = sorting,
            filter = filters,
        )
        mediaPageCalls += page
        return mediaResponses[page].orEmpty()
    }

    override suspend fun getPersonalListShowItems(
        listId: TraktId,
        limit: Int?,
        page: Int,
        extended: String,
        sorting: Sorting,
        filters: GlobalFilter?,
    ): List<ListShowItemDto> {
        capture(
            endpoint = "shows",
            listId = listId,
            page = page,
            limit = limit,
            sorting = sorting,
            filter = filters,
        )
        return emptyList()
    }

    override suspend fun getPersonalListMovieItems(
        listId: TraktId,
        limit: Int?,
        page: Int,
        extended: String,
        sorting: Sorting,
        filters: GlobalFilter?,
    ): List<ListMovieItemDto> {
        capture(
            endpoint = "movies",
            listId = listId,
            page = page,
            limit = limit,
            sorting = sorting,
            filter = filters,
        )
        return emptyList()
    }

    override suspend fun getMovieLists(movieId: TraktId): Set<TraktId> = emptySet()

    override suspend fun getShowLists(showId: TraktId): Set<TraktId> = emptySet()

    private fun capture(
        endpoint: String,
        listId: TraktId,
        page: Int,
        limit: Int?,
        sorting: Sorting,
        filter: GlobalFilter?,
    ) {
        endpointCalls += endpoint
        this.listId = listId
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
