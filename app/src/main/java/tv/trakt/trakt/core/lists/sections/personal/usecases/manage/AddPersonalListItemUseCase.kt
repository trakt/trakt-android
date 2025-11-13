package tv.trakt.trakt.core.lists.sections.personal.usecases.manage

import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserListsLocalDataSource

internal class AddPersonalListItemUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val listsLocalDataSource: ListsPersonalLocalDataSource,
    private val listsItemsLocalDataSource: ListsPersonalItemsLocalDataSource,
    private val userListsLocalDataSource: UserListsLocalDataSource,
) {
    suspend fun addMovie(
        listId: TraktId,
        movie: Movie,
    ) {
        remoteSource.addMovieToList(
            listId = listId,
            movieId = movie.ids.trakt,
        )

        userListsLocalDataSource.addListItem(
            listId = listId,
            item = PersonalListItem.MovieItem(
                movie = movie,
                listedAt = nowUtcInstant(),
            ),
            notify = true,
        )

        listsItemsLocalDataSource.addMovies(
            listId = listId,
            movies = listOf(movie),
            notify = true,
        )
        listsLocalDataSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
    }

    suspend fun addShow(
        listId: TraktId,
        show: Show,
    ) {
        remoteSource.addShowToList(
            listId = listId,
            showId = show.ids.trakt,
        )

        userListsLocalDataSource.addListItem(
            listId = listId,
            item = PersonalListItem.ShowItem(
                show = show,
                listedAt = nowUtcInstant(),
            ),
            notify = true,
        )

        listsItemsLocalDataSource.addShows(
            listId = listId,
            shows = listOf(show),
            notify = true,
        )
        listsLocalDataSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
    }
}
