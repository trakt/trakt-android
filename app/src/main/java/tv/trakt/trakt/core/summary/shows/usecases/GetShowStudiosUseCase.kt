package tv.trakt.trakt.core.summary.shows.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.model.TraktId

internal class GetShowStudiosUseCase(
    private val remoteSource: ShowsRemoteSource,
) {
    suspend fun getStudios(movieId: TraktId): ImmutableList<String> {
        return remoteSource.getStudios(movieId).toImmutableList()
    }
}
