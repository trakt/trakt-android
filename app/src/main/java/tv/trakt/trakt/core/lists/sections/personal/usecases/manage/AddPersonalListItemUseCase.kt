package tv.trakt.trakt.core.lists.sections.personal.usecases.manage

import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.lists.ListsCollaborationsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource

internal class AddPersonalListItemUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val listsLocalDataSource: ListsPersonalLocalDataSource,
    private val collabListsLocalDataSource: ListsCollaborationsLocalDataSource,
) {
    suspend fun addShow(
        listId: TraktId,
        ownerId: TraktId,
        show: Show,
    ) {
        remoteSource.addShowToList(
            userId = ownerId,
            listId = listId,
            showId = show.ids.trakt,
        )

        listsLocalDataSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )

        collabListsLocalDataSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
    }

    suspend fun addMovie(
        listId: TraktId,
        ownerId: TraktId,
        movie: Movie,
    ) {
        remoteSource.addMovieToList(
            userId = ownerId,
            listId = listId,
            movieId = movie.ids.trakt,
        )

        listsLocalDataSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )

        collabListsLocalDataSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
    }
}
