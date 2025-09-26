package tv.trakt.trakt.core.home.sections.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StaticStringResource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.HomeConfig.HOME_WATCHLIST_LIMIT
import tv.trakt.trakt.core.home.sections.watchlist.usecases.AddHomeHistoryUseCase
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetHomeWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.user.usecase.progress.LoadUserProgressUseCase
import java.time.Instant

internal class HomeWatchlistViewModel(
    private val getWatchlistUseCase: GetHomeWatchlistUseCase,
    private val addHistoryUseCase: AddHomeHistoryUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = HomeWatchlistState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var user: User? = null
    private var loadedAt: Instant? = null
    private var processingJob: Job? = null

    init {
        loadData()
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            user = sessionManager.getProfile()
            sessionManager.observeProfile()
                .collect {
                    if (user != it) {
                        user = it
                        loadData()
                    }
                }
        }
    }

    fun loadData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val localItems = getWatchlistUseCase.getLocalWatchlist(HOME_WATCHLIST_LIMIT)
                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getWatchlistUseCase.getWatchlist(HOME_WATCHLIST_LIMIT)
                }

                loadedAt = nowUtcInstant()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.d(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<WatchlistItem.MovieItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { IDLE }
        }

        return false
    }

    fun addToHistory(movieId: TraktId) {
        if (processingJob != null) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                val currentItems = itemsState.value?.toMutableList() ?: return@launch

                val itemIndex = currentItems.indexOfFirst { it.movie.ids.trakt == movieId }
                val itemLoading = currentItems[itemIndex].copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    currentItems.toImmutableList()
                }

                addHistoryUseCase.addToHistory(movieId)
                itemsState.update {
                    getWatchlistUseCase.getWatchlist()
                }
                loadUserProgress()

                infoState.update { StaticStringResource("Added to history") }
                loadedAt = nowUtcInstant()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.d(error, "Failed to add movie to history")
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun loadUserProgress() {
        viewModelScope.launch {
            try {
                loadUserProgressUseCase.loadMoviesProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    override fun onCleared() {
        processingJob?.cancel()
        super.onCleared()
    }

    val state: StateFlow<HomeWatchlistState> = combine(
        itemsState,
        loadingState,
        infoState,
        errorState,
    ) { s1, s2, s3, s4 ->
        HomeWatchlistState(
            items = s1,
            loading = s2,
            info = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
