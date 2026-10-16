package tv.trakt.trakt.core.summary.movies.features.extras.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.common.model.TraktId

internal class GetMovieExtrasUseCase(
    private val remoteSource: MoviesRemoteSource,
) {
    suspend fun getExtraVideos(movieId: TraktId): ImmutableList<ExtraVideo> {
        return remoteSource.getExtras(movieId)
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
