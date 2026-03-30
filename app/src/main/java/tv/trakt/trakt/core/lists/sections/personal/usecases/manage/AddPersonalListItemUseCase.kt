package tv.trakt.trakt.core.lists.sections.personal.usecases.manage

import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.items.ListsCollaborationsItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.lists.ListsCollaborationsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource

internal class AddPersonalListItemUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val listsLocalDataSource: ListsPersonalLocalDataSource,
    private val listsItemsLocalDataSource: ListsPersonalItemsLocalDataSource,
    private val collabListsLocalDataSource: ListsCollaborationsLocalDataSource,
    private val collabListsItemsLocalDataSource: ListsCollaborationsItemsLocalDataSource,
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

        listsItemsLocalDataSource.addShows(
            listId = listId,
            shows = listOf(show),
            notify = true,
        )
        collabListsItemsLocalDataSource.addShows(
            listId = listId,
            shows = listOf(show),
            notify = true,
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

        listsItemsLocalDataSource.addMovies(
            listId = listId,
            movies = listOf(movie),
            notify = true,
        )
        collabListsItemsLocalDataSource.addMovies(
            listId = listId,
            movies = listOf(movie),
            notify = true,
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
