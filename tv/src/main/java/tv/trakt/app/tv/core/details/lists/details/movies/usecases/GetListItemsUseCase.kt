package tv.trakt.app.tv.core.details.lists.details.movies.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.details.lists.data.remote.ListsRemoteDataSource
import tv.trakt.app.tv.core.details.lists.details.CustomListDetailsConfig.CUSTOM_LIST_PAGE_LIMIT
import tv.trakt.app.tv.core.movies.data.local.MovieLocalDataSource
import tv.trakt.app.tv.core.movies.model.Movie
import tv.trakt.app.tv.core.movies.model.fromDto

internal class GetListItemsUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getListItems(
        listId: TraktId,
        page: Int = 1,
    ): ImmutableList<Movie> {
        val movies = remoteSource.getMovieListItems(
            listId = listId,
            limit = CUSTOM_LIST_PAGE_LIMIT,
            page = page,
            extended = "full,images",
        )
            .map { Movie.fromDto(it.movie) }
            .toImmutableList()

        localSource.upsertMovies(movies)

        return movies
    }
}
