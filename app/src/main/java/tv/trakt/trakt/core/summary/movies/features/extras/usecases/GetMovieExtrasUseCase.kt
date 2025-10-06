package tv.trakt.trakt.core.summary.movies.features.extras.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetMovieExtrasUseCase(
    private val remoteSource: MoviesRemoteDataSource,
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
                    .thenBy { it.type }
                    .thenByDescending { it.publishedAt }, // Then by publish date within each type
            )
            .toImmutableList()
    }
}
