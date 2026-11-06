package tv.trakt.trakt.core.settings.features.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.UserHistoryRemoteSource
import tv.trakt.trakt.core.settings.features.history.model.MediaPlays

internal class GetWatchedPlaysUseCase(
    private val remoteSource: UserHistoryRemoteSource,
) {
    suspend fun getMoviePlays(): ImmutableList<MediaPlays> {
        return remoteSource.getWatchedMoviePlays()
            .mapNotNull { (movieId, plays) -> MediaPlays.fromDto(movieId, plays) }
            .toImmutableList()
    }

    suspend fun getEpisodePlays(): ImmutableList<MediaPlays> {
        return remoteSource.getWatchedShowPlays()
            .values
            .flatMap { seasons -> seasons.values }
            .flatMap { episodes -> episodes.entries }
            .mapNotNull { (episodeId, plays) -> MediaPlays.fromDto(episodeId, plays) }
            .toImmutableList()
    }
}
