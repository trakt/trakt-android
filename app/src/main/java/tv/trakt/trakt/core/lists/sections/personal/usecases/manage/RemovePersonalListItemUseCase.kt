package tv.trakt.trakt.core.lists.sections.personal.usecases.manage

import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserListsLocalDataSource

internal class RemovePersonalListItemUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val listsLocalDataSource: ListsPersonalLocalDataSource,
    private val listsItemsLocalDataSource: ListsPersonalItemsLocalDataSource,
    private val userListsLocalDataSource: UserListsLocalDataSource,
) {
    suspend fun removeShow(
        listId: TraktId,
        showId: TraktId,
    ) {
        remoteSource.removeShowFromList(
            listId = listId,
            showId = showId,
        )

        userListsLocalDataSource.removeListItem(
            listId = listId,
            itemId = showId,
            itemType = SHOW,
            notify = true,
        )

        listsItemsLocalDataSource.removeShows(
            listId = listId,
            showsIds = listOf(showId),
            notify = true,
        )
        listsLocalDataSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
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

        userListsLocalDataSource.removeListItem(
            listId = listId,
            itemId = movieId,
            itemType = MOVIE,
            notify = true,
        )

        listsItemsLocalDataSource.removeMovies(
            listId = listId,
            moviesIds = listOf(movieId),
            notify = true,
        )
        listsLocalDataSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
    }
}
