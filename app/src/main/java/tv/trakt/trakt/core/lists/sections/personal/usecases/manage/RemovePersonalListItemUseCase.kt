package tv.trakt.trakt.core.lists.sections.personal.usecases.manage

import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource

internal class RemovePersonalListItemUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val listsLocalDataSource: ListsPersonalLocalDataSource,
    private val listsItemsLocalDataSource: ListsPersonalItemsLocalDataSource,
) {
    suspend fun removeShow(
        listId: TraktId,
        ownerId: TraktId,
        showId: TraktId,
    ) {
        remoteSource.removeShowFromList(
            userId = ownerId,
            listId = listId,
            showId = showId,
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
        ownerId: TraktId,
        movieId: TraktId,
    ) {
        remoteSource.removeMovieFromList(
            userId = ownerId,
            listId = listId,
            movieId = movieId,
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
