package tv.trakt.trakt.app.core.details.movie.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.common.model.TraktId

internal class GetExtraVideosUseCase(
    private val remoteSource: MoviesRemoteSource,
) {
    suspend fun getExtraVideos(movieId: TraktId): ImmutableList<ExtraVideo> {
        val remoteVideos = remoteSource.getExtras(movieId)
            .map { ExtraVideo.fromDto(it) }

        return remoteVideos
            .filter { it.official && it.site == "youtube" && it.url.isNotBlank() }
            .sortedWith(
                compareByDescending<ExtraVideo> { it.type == "trailer" }
                    .thenBy { it.type }
                    .thenByDescending { it.publishedAt }, // Then by publish date within each type
            )
            .toImmutableList()
    }
}
