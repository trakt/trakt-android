package tv.trakt.trakt.core.lists.sections.personal.features.context.item

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
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.local.UserProgressLocalSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.model.CustomListItem.EpisodeItem
import tv.trakt.trakt.core.lists.model.CustomListItem.MovieItem
import tv.trakt.trakt.core.lists.model.CustomListItem.SeasonItem
import tv.trakt.trakt.core.lists.model.CustomListItem.ShowItem
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase

private const val ANALYTICS_SOURCE = "list_episode_context"

@Suppress("UNCHECKED_CAST")
internal class ListItemContextViewModel(
    private val item: CustomListItem,
    private val list: CustomList,
    private val sessionManager: SessionManager,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val userProgressLocalSource: UserProgressLocalSource,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ListItemContextState()

    private val isAuthenticatedState = MutableStateFlow(initialState.isAuthenticated)
    private val isWatchedState = MutableStateFlow(initialState.isWatched)
    private val loadingWatchedState = MutableStateFlow(initialState.loadingWatched)
    private val loadingListState = MutableStateFlow(initialState.loadingList)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadAuthentication()
        loadWatchedStatus()
    }

    private fun loadAuthentication() {
        viewModelScope.launch {
            isAuthenticatedState.update { sessionManager.isAuthenticated() }
        }
    }

    private fun loadWatchedStatus() {
        if (item !is EpisodeItem) return

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingWatchedState.update { Loading }

                if (!userProgressLocalSource.isShowsLoaded()) {
                    loadProgressUseCase.loadShowsProgress()
                }
                isWatchedState.update {
                    userProgressLocalSource.getShows(setOf(item.show.ids.trakt))
                        .firstOrNull()
                        ?.isEpisodeWatched(
                            seasonNumber = item.episode.season,
                            episodeId = item.episode.ids.trakt,
                        ) == true
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingWatchedState.update { Idle }
            }
        }
    }

    fun addToWatched(customDate: DateSelectionResult? = null) {
        if (item !is EpisodeItem || isLoading()) return

        viewModelScope.launch {
            try {
                loadingWatchedState.update { Loading }

                updateEpisodeHistoryUseCase.addToHistory(
                    episodeId = item.episode.ids.trakt,
                    customDate = customDate,
                )

                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = ANALYTICS_SOURCE,
                    date = customDate?.analyticsStrings,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingWatchedState.update { Done }
            }
        }
    }

    fun removeFromWatched() {
        if (item !is EpisodeItem || isLoading()) return

        viewModelScope.launch {
            try {
                loadingWatchedState.update { Loading }

                updateEpisodeHistoryUseCase.removeEpisodeFromHistory(item.episode.ids.trakt.value)

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "episode",
                    source = ANALYTICS_SOURCE,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingWatchedState.update { Done }
            }
        }
    }

    fun removeFromList() {
        if (isLoading()) return

        viewModelScope.launch {
            try {
                loadingListState.update { Loading }

                val listId = list.ids.trakt
                val ownerId = list.user.ids.trakt
                when (item) {
                    is MovieItem -> removeListItemUseCase.removeMovie(listId, ownerId, item.movie.ids.trakt)
                    is ShowItem -> removeListItemUseCase.removeShow(listId, ownerId, item.show.ids.trakt)
                    is SeasonItem -> removeListItemUseCase.removeSeason(listId, ownerId, item.season.ids.trakt)
                    is EpisodeItem -> removeListItemUseCase.removeEpisode(listId, ownerId, item.episode.ids.trakt)
                }
                analytics.lists.logListItemRemove()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingListState.update { Done }
            }
        }
    }

    private fun isLoading(): Boolean {
        return loadingWatchedState.value.isLoading || loadingListState.value.isLoading
    }

    val state = combine(
        isWatchedState,
        loadingWatchedState,
        loadingListState,
        errorState,
        isAuthenticatedState,
    ) { state ->
        ListItemContextState(
            isWatched = state[0] as Boolean,
            loadingWatched = state[1] as LoadingState,
            loadingList = state[2] as LoadingState,
            error = state[3] as Exception?,
            isAuthenticated = state[4] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
