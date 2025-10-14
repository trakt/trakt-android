package tv.trakt.trakt.core.summary.shows.features.extras.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowExtrasUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getExtraVideos(showId: TraktId): ImmutableList<ExtraVideo> {
        return remoteSource.getExtras(showId)
            .map {
                ExtraVideo.fromDto(it)
            }
            .filter {
                it.official &&
                    it.site == "youtube" &&
                    it.url.isNotBlank()
            }
            .sortedWith(
                compareByDescending<ExtraVideo> { it.type == "trailer" }
                    .thenBy { it.type.length },
            )
            .toImmutableList()
    }
}
