package tv.trakt.trakt.core.summary.shows.features.info.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.model.TraktId

internal class GetShowStudiosUseCase(
    private val remoteSource: ShowsRemoteSource,
) {
    suspend fun getStudios(showId: TraktId): ImmutableList<String> {
        return remoteSource.getStudios(showId).toImmutableList()
    }
}
