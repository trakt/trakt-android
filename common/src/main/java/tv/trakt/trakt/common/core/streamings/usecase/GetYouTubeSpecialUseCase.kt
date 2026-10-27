package tv.trakt.trakt.common.core.streamings.usecase

import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.streamings.YouTubeSpecial
import java.util.Locale

class GetYouTubeSpecialUseCase(
    private val remoteSource: MoviesRemoteSource,
) {
    suspend fun getYouTubeSpecial(movieId: TraktId): YouTubeSpecial? {
        return remoteSource
            .getYouTubeSpecial(
                movieId = movieId,
                locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault(),
            )
            ?.let(YouTubeSpecial::fromDto)
    }
}
