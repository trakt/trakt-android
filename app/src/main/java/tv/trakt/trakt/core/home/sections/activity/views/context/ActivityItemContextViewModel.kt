package tv.trakt.trakt.core.home.sections.activity.views.context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.local.AllActivityLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.activity.HomeActivityItem
import tv.trakt.trakt.common.model.activity.HomeActivityItem.EpisodeItem
import tv.trakt.trakt.common.model.activity.HomeActivityItem.MovieItem
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalSource
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase

@Suppress("UNCHECKED_CAST")
internal class ActivityItemContextViewModel(
    private val item: HomeActivityItem,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val activityLocalSource: HomePersonalLocalSource,
    private val allActivityLocalSource: AllActivityLocalSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalSource,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ActivityItemContextState()

    private val isWatchlistState = MutableStateFlow(initialState.isWatchlist)
    private val loadingRemoveState = MutableStateFlow(initialState.loadingRemove)
    private val loadingWatchlistState = MutableStateFlow(initialState.loadingWatchlist)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadWatchlistStatus()
    }

    private fun loadWatchlistStatus() {
        if (item !is MovieItem) return

        viewModelScope.launch {
            try {
                loadingWatchlistState.update { Loading }

                if (!userWatchlistMinLocalSource.isMoviesLoaded()) {
                    loadWatchlistUseCase.loadWatchlist()
                }
                isWatchlistState.update {
                    userWatchlistMinLocalSource.containsMovie(item.movie.ids.trakt)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingWatchlistState.update { Idle }
            }
        }
    }

    fun removePlayFromHistory() {
        if (isLoading()) return
        viewModelScope.launch {
            clear()
            try {
                loadingRemoveState.update { Loading }

                when (item) {
                    is MovieItem -> {
                        updateMovieHistoryUseCase.removePlayFromHistory(
                            playId = item.id,
                            movieId = item.movie.ids.trakt,
                        )
                        analytics.progress.logRemoveWatchedMedia(
                            mediaType = "movie",
                            source = "activity_context",
                        )
                    }

                    is EpisodeItem -> {
                        updateEpisodeHistoryUseCase.removePlayFromHistory(
                            playId = item.id,
                            episodeId = item.episode.ids.trakt,
                            refreshProgress = false,
                        )
                        analytics.progress.logRemoveWatchedMedia(
                            mediaType = "episode",
                            source = "activity_context",
                        )
                        loadUserProgress()
                    }
                }

                activityLocalSource.removeItems(ids = setOf(item.id), notify = true)
                allActivityLocalSource.notifyUpdate()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingRemoveState.update { Done }
            }
        }
    }

    fun addToWatchlist() {
        if (item !is MovieItem || isLoading()) return
        viewModelScope.launch {
            clear()
            try {
                loadingWatchlistState.update { Loading }

                updateMovieWatchlistUseCase.addToWatchlist(item.movie)

                analytics.progress.logAddWatchlistMedia(
                    mediaType = "movie",
                    source = "activity_context",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingWatchlistState.update { Done }
            }
        }
    }

    fun removeFromWatchlist() {
        if (item !is MovieItem || isLoading()) return
        viewModelScope.launch {
            clear()
            try {
                loadingWatchlistState.update { Loading }

                updateMovieWatchlistUseCase.removeFromWatchlist(movieId = item.movie.ids.trakt)

                analytics.progress.logRemoveWatchlistMedia(
                    mediaType = "movie",
                    source = "activity_context",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingWatchlistState.update { Done }
            }
        }
    }

    private fun loadUserProgress() {
        viewModelScope.launch {
            try {
                loadUserProgressUseCase.loadProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun clear() {
        loadingRemoveState.update { Idle }
        loadingWatchlistState.update { Idle }
        errorState.update { null }
    }

    private fun isLoading(): Boolean {
        return loadingRemoveState.value.isLoading || loadingWatchlistState.value.isLoading
    }

    val state = combine(
        isWatchlistState,
        loadingRemoveState,
        loadingWatchlistState,
        errorState,
    ) { state ->
        ActivityItemContextState(
            isWatchlist = state[0] as Boolean,
            loadingRemove = state[1] as LoadingState,
            loadingWatchlist = state[2] as LoadingState,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
