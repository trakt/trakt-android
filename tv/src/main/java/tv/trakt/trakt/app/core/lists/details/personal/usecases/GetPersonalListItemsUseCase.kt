package tv.trakt.trakt.app.core.lists.details.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetPersonalListItemsUseCase(
    private val remoteSource: ProfileRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getListItems(
        listId: TraktId,
        page: Int = 1,
    ): ImmutableList<PersonalListItem> {
        return coroutineScope {
            val showsAsync = async {
                remoteSource.getUserShowListItems(
                    listId = listId,
                    limit = PERSONAL_LIST_PAGE_LIMIT,
                    page = page,
                    extended = "full,cloud9,streaming_ids",
                ).map {
                    PersonalListItem(
                        type = "show",
                        rank = it.rank,
                        show = Show.fromDto(it.show),
                    )
                }.toImmutableList()
            }

            val moviesAsync = async {
                remoteSource.getUserMovieListItems(
                    listId = listId,
                    limit = PERSONAL_LIST_PAGE_LIMIT,
                    page = page,
                    extended = "full,cloud9,streaming_ids",
                ).map {
                    PersonalListItem(
                        type = "movie",
                        rank = it.rank,
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
            }.sortedBy {
                it.rank
            }.toImmutableList()
        }
    }
}
