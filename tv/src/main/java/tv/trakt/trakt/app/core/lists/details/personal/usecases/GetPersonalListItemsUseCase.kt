package tv.trakt.trakt.app.core.lists.details.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.MovieItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.ShowItem
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.usecases.GetListSortRatingsUseCase
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.UserPersonalListsRemoteSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetPersonalListItemsUseCase(
    private val remoteSource: UserPersonalListsRemoteSource,
    private val localShowSource: ShowLocalSource,
    private val localMovieSource: MovieLocalSource,
    private val getListSortRatingsUseCase: GetListSortRatingsUseCase,
) {
    suspend fun getListItems(
        listId: TraktId,
        sorting: Sorting,
        page: Int = 1,
    ): ImmutableList<ListMediaItem> {
        return coroutineScope {
            val showRatingsAsync = async {
                getListSortRatingsUseCase.getShowRatings(sorting, refresh = page == 1)
            }
            val movieRatingsAsync = async {
                getListSortRatingsUseCase.getMovieRatings(sorting, refresh = page == 1)
            }

            val dtos = remoteSource.getPersonalListItems(
                listId = listId,
                limit = PERSONAL_LIST_PAGE_LIMIT,
                page = page,
                extended = "full,streaming_ids",
                sorting = sorting,
            )
            val showRatings = showRatingsAsync.await()
            val movieRatings = movieRatingsAsync.await()

            dtos
                .mapNotNull { ListMediaItem.fromDto(it, showRatings, movieRatings) }
                .toImmutableList()
                .also {
                    localShowSource.upsertShows(it.filterIsInstance<ShowItem>().map { item -> item.show })
                    localMovieSource.upsertMovies(it.filterIsInstance<MovieItem>().map { item -> item.movie })
                }
        }
    }
}
