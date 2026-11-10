package tv.trakt.trakt.helpers.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.data.local.EpisodeLocalSource
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

internal class MediaNavigationDelegate private constructor(
    private val showLocalSource: ShowLocalSource,
    private val movieLocalSource: MovieLocalSource,
    private val episodeLocalSource: EpisodeLocalSource,
    private val scope: CoroutineScope,
    private val isBusy: () -> Boolean,
) {
    private val showState = MutableStateFlow<TraktId?>(null)
    private val movieState = MutableStateFlow<TraktId?>(null)
    private val episodeState = MutableStateFlow<Pair<TraktId, Episode>?>(null)

    private var job: Job? = null

    val show = showState.asStateFlow()
    val movie = movieState.asStateFlow()
    val episode = episodeState.asStateFlow()

    fun toShow(show: Show) {
        if (showState.value != null) {
            return
        }
        navigate {
            showLocalSource.upsertShows(listOf(show))
            showState.update { show.ids.trakt }
        }
    }

    fun toMovie(movie: Movie) {
        if (movieState.value != null) {
            return
        }
        navigate {
            movieLocalSource.upsertMovies(listOf(movie))
            movieState.update { movie.ids.trakt }
        }
    }

    fun toEpisode(
        show: Show,
        episode: Episode,
    ) {
        if (episodeState.value != null) {
            return
        }
        navigate {
            showLocalSource.upsertShows(listOf(show))
            episodeLocalSource.upsertEpisodes(listOf(episode))
            episodeState.update { show.ids.trakt to episode }
        }
    }

    fun clear() {
        showState.update { null }
        movieState.update { null }
        episodeState.update { null }
    }

    private fun navigate(block: suspend () -> Unit) {
        if (job?.isActive == true || isBusy()) {
            return
        }
        job = scope.launch {
            try {
                block()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    class Factory(
        private val showLocalSource: ShowLocalSource,
        private val movieLocalSource: MovieLocalSource,
        private val episodeLocalSource: EpisodeLocalSource,
    ) {
        fun create(
            scope: CoroutineScope,
            isBusy: () -> Boolean = { false },
        ): MediaNavigationDelegate {
            return MediaNavigationDelegate(
                showLocalSource = showLocalSource,
                movieLocalSource = movieLocalSource,
                episodeLocalSource = episodeLocalSource,
                scope = scope,
                isBusy = isBusy,
            )
        }
    }
}
