package tv.trakt.trakt.core.calendar.usecases

import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show

/**
 * Caches the media a calendar item points at before navigating to its details, so
 * the destination screen renders from local data instead of an empty state.
 */
internal class SaveCalendarMediaUseCase(
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
) {
    suspend fun saveShow(show: Show) {
        showLocalDataSource.upsertShows(listOf(show))
    }

    suspend fun saveMovie(movie: Movie) {
        movieLocalDataSource.upsertMovies(listOf(movie))
    }

    suspend fun saveEpisode(
        show: Show,
        episode: Episode,
    ) {
        showLocalDataSource.upsertShows(listOf(show))
        episodeLocalDataSource.upsertEpisodes(listOf(episode))
    }
}
