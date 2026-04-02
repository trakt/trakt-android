package tv.trakt.trakt.core.summary.shows.features.info.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowStudiosUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getStudios(showId: TraktId): ImmutableList<String> {
        return remoteSource.getStudios(showId).toImmutableList()
    }
}
