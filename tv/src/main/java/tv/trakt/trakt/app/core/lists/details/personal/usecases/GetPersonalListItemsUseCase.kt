package tv.trakt.trakt.app.core.lists.details.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.openapitools.client.models.GetUsersWatchlistAll200ResponseInner.Type
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem
import tv.trakt.trakt.app.core.lists.usecases.GetListSortRatingsUseCase
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.UserPersonalListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ListMediaItemDto

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
    ): ImmutableList<PersonalListItem> {
        return coroutineScope {
            val showRatingsAsync = async {
                getListSortRatingsUseCase.getShowRatings(sorting, refresh = page == 1)
            }
            val movieRatingsAsync = async {
                getListSortRatingsUseCase.getMovieRatings(sorting, refresh = page == 1)
            }

            val dtos = remoteSource.getPersonalListMediaItems(
                listId = listId,
                limit = PERSONAL_LIST_PAGE_LIMIT,
                page = page,
                extended = "full,streaming_ids",
                sorting = sorting,
            )
            val showRatings = showRatingsAsync.await()
            val movieRatings = movieRatingsAsync.await()

            mapItems(dtos, showRatings, movieRatings).also {
                localShowSource.upsertShows(it.mapNotNull { item -> item.show })
                localMovieSource.upsertMovies(it.mapNotNull { item -> item.movie })
            }
        }
    }

    private fun mapItems(
        dtos: List<ListMediaItemDto>,
        showRatings: Map<TraktId, UserRating>,
        movieRatings: Map<TraktId, UserRating>,
    ): ImmutableList<PersonalListItem> {
        return dtos.mapNotNull { dto ->
            when (dto.type) {
                Type.SHOW -> {
                    val show = dto.show?.let { Show.fromDto(it) } ?: return@mapNotNull null
                    PersonalListItem(
                        type = "show",
                        rank = dto.rank,
                        show = show,
                        listedAt = dto.listedAt.toInstant(),
                        userRating = showRatings[show.ids.trakt],
                    )
                }

                Type.MOVIE -> {
                    val movie = dto.movie?.let { Movie.fromDto(it) } ?: return@mapNotNull null
                    PersonalListItem(
                        type = "movie",
                        rank = dto.rank,
                        movie = movie,
                        listedAt = dto.listedAt.toInstant(),
                        userRating = movieRatings[movie.ids.trakt],
                    )
                }
            }
        }.toImmutableList()
    }
}
