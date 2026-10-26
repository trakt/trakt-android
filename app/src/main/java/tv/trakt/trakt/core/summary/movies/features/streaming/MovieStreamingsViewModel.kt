package tv.trakt.trakt.core.summary.movies.features.streaming

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.streamings.model.StreamingsRequest
import tv.trakt.trakt.common.core.streamings.model.StreamingsResult
import tv.trakt.trakt.common.core.streamings.usecase.GetStreamingsUseCase
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.YouTubeSpecial
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import java.util.Locale

@Suppress("UNCHECKED_CAST")
internal class MovieStreamingsViewModel(
    private val movie: Movie,
    private val sessionManager: SessionManager,
    private val getStreamingsUseCase: GetStreamingsUseCase,
    private val moviesRemoteSource: MoviesRemoteSource,
    private val collapsingManager: CollapsingManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = MovieStreamingsState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val youTubeSpecialState = MutableStateFlow(initialState.youTubeSpecial)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.MOVIE_WHERE_TO_WATCH))
    private val errorState = MutableStateFlow(initialState.error)

    private var collapseJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val user = sessionManager.getProfile()
                if (user == null) {
                    itemsState.update { null }
                    loadingState.update { Done }
                    return@launch
                }

                loadYouTubeSpecial()

                val items = getStreamingsUseCase.getStreamings(
                    user = user,
                    request = StreamingsRequest(
                        mediaType = MediaType.Movie,
                        mediaId = movie.ids.trakt,
                    ),
                )

                itemsState.update { items }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private fun loadYouTubeSpecial() {
        if (MediaGenre.Comedy !in movie.genres) {
            return
        }

        viewModelScope.launch {
            try {
                val special = moviesRemoteSource
                    .getYouTubeSpecial(
                        movieId = movie.ids.trakt,
                        locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault(),
                    )
                    ?.let(YouTubeSpecial::fromDto)
                youTubeSpecialState.update { special }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.MOVIE_WHERE_TO_WATCH)
                else -> collapsingManager.expand(CollapsingKey.MOVIE_WHERE_TO_WATCH)
            }
        }
    }

    fun logStreamOn(service: StreamingService) {
        analytics.playback.logStreamOn(service.source)
    }

    fun logYouTubeSpecialStreamOn() {
        analytics.playback.logStreamOn(YOUTUBE_SOURCE)
    }

    val state = combine(
        itemsState,
        loadingState,
        collapseState,
        errorState,
        youTubeSpecialState,
    ) { state ->
        MovieStreamingsState(
            items = state[0] as StreamingsResult?,
            loading = state[1] as LoadingState,
            collapsed = state[2] as Boolean,
            error = state[3] as Exception?,
            youTubeSpecial = state[4] as YouTubeSpecial?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

private const val YOUTUBE_SOURCE = "youtube"
