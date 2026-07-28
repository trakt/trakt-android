package tv.trakt.trakt.core.search.usecase.popular

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.search.data.remote.SearchRemoteDataSource
import tv.trakt.trakt.common.model.TraktId

internal class PostUserSearchUseCase(
    private val remoteSource: SearchRemoteDataSource,
    private val sessionManager: SessionManager,
) {
    suspend fun postShowUserSearch(
        showId: TraktId,
        query: String,
    ) {
        if (!sessionManager.isAuthenticated()) {
            return
        }
        remoteSource.postShowUserSearch(
            showId = showId,
            query = query,
        )
    }

    suspend fun postMovieUserSearch(
        movieId: TraktId,
        query: String,
    ) {
        if (!sessionManager.isAuthenticated()) {
            return
        }
        remoteSource.postMovieUserSearch(
            movieId = movieId,
            query = query,
        )
    }

    suspend fun postPersonUserSearch(
        personId: TraktId,
        query: String,
    ) {
        if (!sessionManager.isAuthenticated()) {
            return
        }
        remoteSource.postPersonUserSearch(
            personId = personId,
            query = query,
        )
    }

    suspend fun postListUserSearch(
        listId: TraktId,
        query: String,
    ) {
        if (!sessionManager.isAuthenticated()) {
            return
        }
        remoteSource.postListUserSearch(
            listId = listId,
            query = query,
        )
    }
}
