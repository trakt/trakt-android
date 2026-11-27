package tv.trakt.trakt.core.mixtape.usecases

import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.mixtape.MixtapeItem

internal class GetMixtapePickUseCase(
    private val moviesRemoteSource: MoviesRemoteSource,
    private val showsRemoteSource: ShowsRemoteSource,
) {
    suspend fun getPick(
        type: MediaType,
        id: TraktId,
    ): MixtapeItem? {
        return when (type) {
            MediaType.Movie -> MixtapeItem.MovieItem(Movie.fromDto(moviesRemoteSource.getDetails(id)))
            MediaType.Show -> MixtapeItem.ShowItem(Show.fromDto(showsRemoteSource.getShowDetails(id)))
            MediaType.Season, MediaType.Episode -> null
        }
    }
}
