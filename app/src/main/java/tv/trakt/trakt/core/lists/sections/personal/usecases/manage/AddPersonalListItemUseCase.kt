package tv.trakt.trakt.core.lists.sections.personal.usecases.manage

import tv.trakt.trakt.common.data.local.ListsCollaborationsLocalSource
import tv.trakt.trakt.common.data.local.ListsPersonalLocalSource
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

internal class AddPersonalListItemUseCase(
    private val remoteSource: ListsRemoteSource,
    private val listsLocalSource: ListsPersonalLocalSource,
    private val collabListsLocalSource: ListsCollaborationsLocalSource,
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

        listsLocalSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )

        collabListsLocalSource.onUpdatedAt(
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

        listsLocalSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )

        collabListsLocalSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
    }
}
