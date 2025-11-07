package tv.trakt.trakt.core.summary.shows.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowStudiosUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getStudios(movieId: TraktId): ImmutableList<String> {
        return remoteSource.getStudios(movieId).toImmutableList()
    }
}
