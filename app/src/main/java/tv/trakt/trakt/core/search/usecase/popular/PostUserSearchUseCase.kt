package tv.trakt.trakt.core.search.usecase.popular

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.search.data.remote.SearchRemoteDataSource

internal class PostUserSearchUseCase(
    private val remoteSource: SearchRemoteDataSource,
) {
    suspend fun postShowUserSearch(
        showId: TraktId,
        query: String,
    ) {
        remoteSource.postShowUserSearch(
            showId = showId,
            query = query,
        )
    }

    suspend fun postMovieUserSearch(
        movieId: TraktId,
        query: String,
    ) {
        remoteSource.postMovieUserSearch(
            movieId = movieId,
            query = query,
        )
    }
}
