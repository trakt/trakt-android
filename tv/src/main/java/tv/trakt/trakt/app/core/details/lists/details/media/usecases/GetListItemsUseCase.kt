package tv.trakt.trakt.app.core.details.lists.details.media.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.openapitools.client.models.GetUsersWatchlistAll200ResponseInner.Type
import tv.trakt.trakt.app.core.details.lists.details.CustomListDetailsConfig.CUSTOM_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.MovieItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.ShowItem
import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetListItemsUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val showLocalSource: ShowLocalDataSource,
    private val movieLocalSource: MovieLocalDataSource,
) {
    suspend fun getListItems(
        listId: TraktId,
        page: Int = 1,
    ): ImmutableList<ListMediaItem> {
        val dto = remoteSource.getMediaListItems(
            listId = listId,
            extended = "full,cloud9,streaming_ids",
            pagination = Pagination(page, CUSTOM_LIST_PAGE_LIMIT),
            sorting = Sorting.Default,
        )

        val shows = mutableListOf<Show>()
        val movies = mutableListOf<Movie>()

        val items = dto.mapNotNull { dto ->
            when (dto.type) {
                Type.SHOW -> {
                    val show = dto.show?.let { Show.fromDto(it) } ?: return@mapNotNull null
                    shows.add(show)
                    ShowItem(show)
                }

                Type.MOVIE -> {
                    val movie = dto.movie?.let { Movie.fromDto(it) } ?: return@mapNotNull null
                    movies.add(movie)
                    MovieItem(movie)
                }
            }
        }.toImmutableList()

        return items.also {
            showLocalSource.upsertShows(shows)
            movieLocalSource.upsertMovies(movies)
        }
    }

    suspend fun getShowListItems(
        listId: TraktId,
        page: Int = 1,
    ): ImmutableList<ListMediaItem> {
        return remoteSource.getShowListItems(
            listId = listId,
            extended = "full,cloud9,streaming_ids",
            pagination = Pagination(page, CUSTOM_LIST_PAGE_LIMIT),
            sorting = Sorting.Default,
        )
            .map {
                val show = Show.fromDto(it.show)
                ShowItem(show)
            }
            .toImmutableList()
            .also {
                val shows = it.map { item -> item.show }
                showLocalSource.upsertShows(shows)
            }
    }

    suspend fun getMovieListItems(
        listId: TraktId,
        page: Int = 1,
    ): ImmutableList<ListMediaItem> {
        return remoteSource.getMovieListItems(
            listId = listId,
            extended = "full,cloud9,streaming_ids",
            pagination = Pagination(page, CUSTOM_LIST_PAGE_LIMIT),
            sorting = Sorting.Default,
        )
            .map {
                val movie = Movie.fromDto(it.movie)
                MovieItem(movie)
            }
            .toImmutableList()
            .also {
                val movies = it.map { item -> item.movie }
                movieLocalSource.upsertMovies(movies)
            }
    }
}
