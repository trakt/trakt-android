package tv.trakt.trakt.core.summary.movies.features.info.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.model.TraktId

internal class GetMovieStudiosUseCase(
    private val remoteSource: MoviesRemoteSource,
) {
    suspend fun getStudios(movieId: TraktId): ImmutableList<String> {
        return remoteSource.getStudios(movieId).toImmutableList()
    }
}
