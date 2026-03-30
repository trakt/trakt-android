package tv.trakt.trakt.core.lists.sections.personal.usecases.manage

import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.items.ListsCollaborationsItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.lists.ListsCollaborationsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource

internal class RemovePersonalListItemUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val personalListsLocalDataSource: ListsPersonalLocalDataSource,
    private val personalListsItemsLocalDataSource: ListsPersonalItemsLocalDataSource,
    private val collabListsLocalDataSource: ListsCollaborationsLocalDataSource,
    private val collabListsItemsLocalDataSource: ListsCollaborationsItemsLocalDataSource,
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

        personalListsItemsLocalDataSource.removeShows(
            listId = listId,
            showsIds = listOf(showId),
            notify = true,
        )
        collabListsItemsLocalDataSource.removeShows(
            listId = listId,
            showsIds = listOf(showId),
            notify = true,
        )

        personalListsLocalDataSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
        collabListsLocalDataSource.onUpdatedAt(
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

        personalListsItemsLocalDataSource.removeMovies(
            listId = listId,
            moviesIds = listOf(movieId),
            notify = true,
        )
        collabListsItemsLocalDataSource.removeMovies(
            listId = listId,
            moviesIds = listOf(movieId),
            notify = true,
        )

        personalListsLocalDataSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
        collabListsLocalDataSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
    }
}
