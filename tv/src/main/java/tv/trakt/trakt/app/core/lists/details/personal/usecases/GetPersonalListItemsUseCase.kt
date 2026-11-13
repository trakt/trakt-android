package tv.trakt.trakt.app.core.lists.details.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.openapitools.client.models.GetUsersWatchlistAll200ResponseInner.Type
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.UserPersonalListsRemoteSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetPersonalListItemsUseCase(
    private val remoteSource: UserPersonalListsRemoteSource,
    private val localShowSource: ShowLocalSource,
    private val localMovieSource: MovieLocalSource,
) {
    suspend fun getListItems(
        listId: TraktId,
        sorting: Sorting,
        page: Int = 1,
    ): ImmutableList<PersonalListItem> {
        val items = remoteSource.getPersonalListMediaItems(
            listId = listId,
            limit = PERSONAL_LIST_PAGE_LIMIT,
            page = page,
            extended = "full,streaming_ids",
            sorting = sorting,
        ).mapNotNull { dto ->
            when (dto.type) {
                Type.SHOW -> {
                    val show = dto.show?.let { Show.fromDto(it) } ?: return@mapNotNull null
                    PersonalListItem(
                        type = "show",
                        rank = dto.rank,
                        show = show,
                    )
                }

                Type.MOVIE -> {
                    val movie = dto.movie?.let { Movie.fromDto(it) } ?: return@mapNotNull null
                    PersonalListItem(
                        type = "movie",
                        rank = dto.rank,
                        movie = movie,
                    )
                }
            }
        }.toImmutableList()

        return items.also {
            localShowSource.upsertShows(it.mapNotNull { item -> item.show })
            localMovieSource.upsertMovies(it.mapNotNull { item -> item.movie })
        }
    }
}
