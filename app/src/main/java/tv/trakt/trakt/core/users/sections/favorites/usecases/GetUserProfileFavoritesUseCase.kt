package tv.trakt.trakt.core.users.sections.favorites.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.favorites.UserFavoritesRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.favorites.model.getFavoriteSorting

internal class GetUserProfileFavoritesUseCase(
    private val remoteSource: UserFavoritesRemoteDataSource,
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

            val shows = showsAsync?.await()?.asyncMap {
                FavoriteItem.ShowItem(
                    show = Show.fromDto(it.show),
                    rank = it.rank,
                    listedAt = it.listedAt.toInstant(),
                )
            } ?: emptyList()

            val movies = moviesAsync?.await()?.asyncMap {
                FavoriteItem.MovieItem(
                    movie = Movie.fromDto(it.movie),
                    rank = it.rank,
                    listedAt = it.listedAt.toInstant(),
                )
            } ?: emptyList()

            (shows + movies)
                .sortedWith(getFavoriteSorting(sorting))
                .toImmutableList()
        }
    }
}
