package tv.trakt.trakt.core.lists.sections.personal.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource

internal class RemovePersonalListItemUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val localSource: ListsPersonalItemsLocalDataSource,
) {
    suspend fun removeShow(
        listId: TraktId,
        showId: TraktId,
    ) {
        remoteSource.removeShowFromList(
            listId = listId,
            showId = showId,
        )
        localSource.removeShows(
            listId = listId,
            showsIds = listOf(showId),
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
        localSource.removeMovies(
            listId = listId,
            moviesIds = listOf(movieId),
        )
    }
}
