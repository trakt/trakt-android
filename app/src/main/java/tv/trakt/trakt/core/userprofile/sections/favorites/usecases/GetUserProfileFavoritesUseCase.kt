package tv.trakt.trakt.core.userprofile.sections.favorites.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.core.favorites.getFavoriteSorting
import tv.trakt.trakt.common.core.user.data.remote.favorites.UserFavoritesRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase

internal class GetUserProfileFavoritesUseCase(
    private val remoteSource: UserFavoritesRemoteDataSource,
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
) {
    suspend fun getUserFavorites(
        userId: TraktId,
        mode: MediaMode? = null,
        sorting: Sorting? = null,
    ): ImmutableList<FavoriteItem> {
        return coroutineScope {
            val showsAsync = if (mode == null || mode.isMediaOrShows) {
                async {
                    remoteSource.getFavoriteShows(
                        userId = userId.value.toString(),
                        sorting = sorting,
                        extended = "full,cloud9,colors",
                    )
                }
            } else {
                null
            }

            val moviesAsync = if (mode == null || mode.isMediaOrMovies) {
                async {
                    remoteSource.getFavoriteMovies(
                        userId = userId.value.toString(),
                        sorting = sorting,
                        extended = "full,cloud9,colors",
                    )
                }
            } else {
                null
            }

            val (showsRatings, moviesRatings) = loadUserRatingsUseCase.loadAllIfNeeded()

            val shows = showsAsync?.await()?.asyncMap {
                val show = Show.fromDto(it.show)
                FavoriteItem.ShowItem(
                    show = show,
                    rank = it.rank,
                    listedAt = it.listedAt.toInstant(),
                    userRating = showsRatings[show.ids.trakt],
                )
            } ?: emptyList()

            val movies = moviesAsync?.await()?.asyncMap {
                val movie = Movie.fromDto(it.movie)
                FavoriteItem.MovieItem(
                    movie = movie,
                    rank = it.rank,
                    listedAt = it.listedAt.toInstant(),
                    userRating = moviesRatings[movie.ids.trakt],
                )
            } ?: emptyList()

            (shows + movies)
                .sortedWith(getFavoriteSorting(sorting))
                .toImmutableList()
        }
    }
}
