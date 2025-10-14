package tv.trakt.trakt.core.lists.sections.personal.usecases

import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserListsLocalDataSource

internal class RemovePersonalListItemUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val listsItemsLocalDataSource: ListsPersonalItemsLocalDataSource,
    private val listsLocalDataSource: UserListsLocalDataSource,
) {
    suspend fun removeShow(
        listId: TraktId,
        showId: TraktId,
    ) {
        remoteSource.removeShowFromList(
            listId = listId,
            showId = showId,
        )

        listsItemsLocalDataSource.removeShows(
            listId = listId,
            showsIds = listOf(showId),
            notify = true,
        )

        listsLocalDataSource.removeListItem(
            listId = listId,
            itemId = showId,
            itemType = SHOW,
            notify = true,
        )
    }

    suspend fun removeMovie(
        listId: TraktId,
        movieId: TraktId,
    ) {
        remoteSource.removeMovieFromList(
            listId = listId,
            movieId = movieId,
        )

        listsItemsLocalDataSource.removeMovies(
            listId = listId,
            moviesIds = listOf(movieId),
            notify = true,
        )

        listsLocalDataSource.removeListItem(
            listId = listId,
            itemId = movieId,
            itemType = MOVIE,
            notify = true,
        )
    }
}
