package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting

/**
 * Provides the user's ratings only when a list is sorted by them. Pass [refresh] for the first
 * page so later pages reuse the ratings already loaded.
 */
internal class GetListSortRatingsUseCase(
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
) {
    suspend fun getShowRatings(
        sorting: Sorting,
        refresh: Boolean,
    ): ImmutableMap<TraktId, UserRating> {
        if (sorting.type != SortType.UserRating) return persistentMapOf()
        if (refresh || !loadUserRatingsUseCase.isShowsLoaded()) return loadUserRatingsUseCase.loadShows()

        return loadUserRatingsUseCase.loadLocalShows()
    }

    suspend fun getMovieRatings(
        sorting: Sorting,
        refresh: Boolean,
    ): ImmutableMap<TraktId, UserRating> {
        if (sorting.type != SortType.UserRating) return persistentMapOf()
        if (refresh || !loadUserRatingsUseCase.isMoviesLoaded()) return loadUserRatingsUseCase.loadMovies()

        return loadUserRatingsUseCase.loadLocalMovies()
    }
}
