package tv.trakt.app.tv.core.details.movie.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.common.model.ExtraVideo
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.movies.data.remote.MoviesRemoteDataSource

internal class GetExtraVideosUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getExtraVideos(movieId: TraktId): ImmutableList<ExtraVideo> {
        val remoteVideos = remoteSource.getMovieExtras(movieId)
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
