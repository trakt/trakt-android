package tv.trakt.trakt.core.calendar.usecases

import tv.trakt.trakt.common.data.local.EpisodeLocalSource
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show

/**
 * Caches the media a calendar item points at before navigating to its details, so
 * the destination screen renders from local data instead of an empty state.
 */
internal class SaveCalendarMediaUseCase(
    private val showLocalSource: ShowLocalSource,
    private val movieLocalSource: MovieLocalSource,
    private val episodeLocalSource: EpisodeLocalSource,
) {
    suspend fun saveShow(show: Show) {
        showLocalSource.upsertShows(listOf(show))
    }

    suspend fun saveMovie(movie: Movie) {
        movieLocalSource.upsertMovies(listOf(movie))
    }

    suspend fun saveEpisode(
        show: Show,
        episode: Episode,
    ) {
        showLocalSource.upsertShows(listOf(show))
        episodeLocalSource.upsertEpisodes(listOf(episode))
    }
}
