package tv.trakt.trakt.core.lists.sections.personal.usecases

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserListsLocalDataSource

internal class AddPersonalListItemUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val listsItemsLocalDataSource: ListsPersonalItemsLocalDataSource,
    private val listsLocalDataSource: UserListsLocalDataSource,
) {
    suspend fun addMovie(
        listId: TraktId,
        movie: Movie,
    ) {
        remoteSource.addMovieToList(
            listId = listId,
            movieId = movie.ids.trakt,
        )

        listsItemsLocalDataSource.addMovies(
            listId = listId,
            movies = listOf(movie),
            notify = true,
        )

        listsLocalDataSource.addListItem(
            listId = listId,
            item = PersonalListItem.MovieItem(
                movie = movie,
                listedAt = nowUtcInstant(),
            ),
            notify = true,
        )
    }
}
