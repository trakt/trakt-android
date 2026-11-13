package tv.trakt.trakt.app.core.profile.sections.favorites.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.lists.usecases.GetListSortRatingsUseCase
import tv.trakt.trakt.app.core.profile.sections.favorites.model.FavoriteItem
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.remote.UserFavoritesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetFavoriteMoviesUseCase(
    private val remoteUserSource: UserFavoritesRemoteSource,
    private val localMoviesSource: MovieLocalSource,
    private val getListSortRatingsUseCase: GetListSortRatingsUseCase,
) {
    suspend fun getFavoriteMovies(
        sorting: Sorting,
        page: Int = 1,
        limit: Int,
    ): ImmutableList<FavoriteItem.MovieItem> {
        return coroutineScope {
            val ratingsAsync = async {
                getListSortRatingsUseCase.getMovieRatings(sorting, refresh = true)
            }

            val movies = remoteUserSource.getFavoriteMovies(
                extended = "full,colors,streaming_ids",
                sorting = sorting,
                pagination = Pagination(page = page, limit = limit),
            ).asyncMap { Movie.fromDto(it.movie) to it }

            val ratings = ratingsAsync.await()
            movies
                .map { (movie, dto) ->
                    FavoriteItem.MovieItem(
                        movie = movie,
                        rank = dto.rank,
                        listedAt = dto.listedAt.toInstant(),
                        userRating = ratings[movie.ids.trakt],
                    )
                }
                .toImmutableList()
                .also {
                    localMoviesSource.upsertMovies(it.map { item -> item.movie })
                }
        }
    }
}
