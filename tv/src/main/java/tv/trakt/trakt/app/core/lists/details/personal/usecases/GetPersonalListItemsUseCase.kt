package tv.trakt.trakt.app.core.lists.details.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem
import tv.trakt.trakt.app.core.lists.details.personal.model.getPersonalListSorting
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.UserPersonalListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.toInstant
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
        return coroutineScope {
            val showsAsync = async {
                remoteSource.getPersonalListShowItems(
                    listId = listId,
                    limit = PERSONAL_LIST_PAGE_LIMIT,
                    page = page,
                    extended = "full,streaming_ids",
                    sorting = sorting,
                ).map {
                    PersonalListItem(
                        type = "show",
                        rank = it.rank,
                        listedAt = it.listedAt.toInstant(),
                        show = Show.fromDto(it.show),
                    )
                }.toImmutableList()
            }

            val moviesAsync = async {
                remoteSource.getPersonalListMovieItems(
                    listId = listId,
                    limit = PERSONAL_LIST_PAGE_LIMIT,
                    page = page,
                    extended = "full,streaming_ids",
                    sorting = sorting,
                ).map {
                    PersonalListItem(
                        type = "movie",
                        rank = it.rank,
                        listedAt = it.listedAt.toInstant(),
                        movie = Movie.fromDto(it.movie),
                    )
                }.toImmutableList()
            }

            val shows = showsAsync.await()
            val movies = moviesAsync.await()

            val localShowsAsync = async {
                shows.mapNotNull { it.show }.let {
                    localShowSource.upsertShows(it)
                }
            }
            val localMoviesAsync = async {
                movies.mapNotNull { it.movie }.let {
                    localMovieSource.upsertMovies(it)
                }
            }

            localShowsAsync.await()
            localMoviesAsync.await()

            buildList {
                addAll(shows)
                addAll(movies)
            }.sortedWith(
                getPersonalListSorting(sorting),
            ).toImmutableList()
        }
    }
}
