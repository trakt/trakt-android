package tv.trakt.trakt.core.lists.sections.personal.usecases.manage

import tv.trakt.trakt.common.data.local.ListsCollaborationsLocalSource
import tv.trakt.trakt.common.data.local.ListsPersonalLocalSource
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.TraktId

internal class RemovePersonalListItemUseCase(
    private val remoteSource: ListsRemoteSource,
    private val personalListsLocalSource: ListsPersonalLocalSource,
    private val collabListsLocalSource: ListsCollaborationsLocalSource,
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

        personalListsLocalSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )

        collabListsLocalSource.onUpdatedAt(
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

        personalListsLocalSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )

        collabListsLocalSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
    }

    suspend fun removeSeason(
        listId: TraktId,
        ownerId: TraktId,
        seasonId: TraktId,
    ) {
        remoteSource.removeSeasonFromList(
            userId = ownerId,
            listId = listId,
            seasonId = seasonId,
        )

        personalListsLocalSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )

        collabListsLocalSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
    }

    suspend fun removeEpisode(
        listId: TraktId,
        ownerId: TraktId,
        episodeId: TraktId,
    ) {
        remoteSource.removeEpisodeFromList(
            userId = ownerId,
            listId = listId,
            episodeId = episodeId,
        )

        personalListsLocalSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )

        collabListsLocalSource.onUpdatedAt(
            id = listId,
            updatedAt = nowUtc(),
        )
    }
}
