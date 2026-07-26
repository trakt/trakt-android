package tv.trakt.trakt.app.core.details.lists.details.media.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.openapitools.client.models.GetUsersWatchlistAll200ResponseInner.Type
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.MovieItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.ShowItem
import tv.trakt.trakt.app.core.lists.filters.TvListPage
import tv.trakt.trakt.app.core.lists.filters.TvListRequest
import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination

internal class GetListItemsUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val showLocalSource: ShowLocalDataSource,
    private val movieLocalSource: MovieLocalDataSource,
) {
    suspend fun getListItems(
        listId: TraktId,
        request: TvListRequest,
    ): TvListPage<ListMediaItem> {
        return when (request.filter.mode) {
            MediaMode.Media -> getMediaItems(listId, request)
            MediaMode.Shows -> getShowItems(listId, request)
            MediaMode.Movies -> getMovieItems(listId, request)
        }
    }

    private suspend fun getMediaItems(
        listId: TraktId,
        request: TvListRequest,
    ): TvListPage<ListMediaItem> {
        val response = remoteSource.getMediaListItems(
            listId = listId,
            extended = EXTENDED,
            pagination = request.pagination,
            sorting = request.sorting,
            filters = request.filter,
        )
        val items = response.mapNotNull { dto ->
            when (dto.type) {
                Type.SHOW -> dto.show?.let { ShowItem(Show.fromDto(it)) }
                Type.MOVIE -> dto.movie?.let { MovieItem(Movie.fromDto(it)) }
            }
        }.toImmutableList()

        persist(items)

        return request.toPage(
            items = items,
            responseSize = response.size,
        )
    }

    private suspend fun getShowItems(
        listId: TraktId,
        request: TvListRequest,
    ): TvListPage<ListMediaItem> {
        val response = remoteSource.getShowListItems(
            listId = listId,
            extended = EXTENDED,
            pagination = request.pagination,
            sorting = request.sorting,
            filters = request.filter,
        )
        val items = response
            .map { ShowItem(Show.fromDto(it.show)) }
            .toImmutableList()

        persist(items)

        return request.toPage(
            items = items,
            responseSize = response.size,
        )
    }

    private suspend fun getMovieItems(
        listId: TraktId,
        request: TvListRequest,
    ): TvListPage<ListMediaItem> {
        val response = remoteSource.getMovieListItems(
            listId = listId,
            extended = EXTENDED,
            pagination = request.pagination,
            sorting = request.sorting,
            filters = request.filter,
        )
        val items = response
            .map { MovieItem(Movie.fromDto(it.movie)) }
            .toImmutableList()

        persist(items)

        return request.toPage(
            items = items,
            responseSize = response.size,
        )
    }

    private suspend fun persist(items: ImmutableList<ListMediaItem>) {
        showLocalSource.upsertShows(
            items.mapNotNull { (it as? ShowItem)?.show },
        )
        movieLocalSource.upsertMovies(
            items.mapNotNull { (it as? MovieItem)?.movie },
        )
    }

    private fun TvListRequest.toPage(
        items: ImmutableList<ListMediaItem>,
        responseSize: Int,
    ): TvListPage<ListMediaItem> {
        return TvListPage(
            items = items,
            nextPage = page + 1,
            hasMore = responseSize >= limit,
        )
    }

    private val TvListRequest.pagination: Pagination
        get() = Pagination(page, limit)

    private companion object {
        const val EXTENDED = "full,cloud9,streaming_ids"
    }
}
