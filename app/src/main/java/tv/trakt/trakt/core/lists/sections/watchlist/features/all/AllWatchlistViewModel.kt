package tv.trakt.trakt.core.lists.sections.watchlist.features.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.ImmutableList
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
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetHomeWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation.ListsWatchlistDestination
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistFilter
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistFilter.MEDIA
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistFilter.MOVIES
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistFilter.SHOWS
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetMoviesWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.filters.GetWatchlistFilterUseCase

internal class AllWatchlistViewModel(
    savedStateHandle: SavedStateHandle,
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val getHomeWatchlistUseCase: GetHomeWatchlistUseCase,
    private val getShowsWatchlistUseCase: GetShowsWatchlistUseCase,
    private val getMoviesWatchlistUseCase: GetMoviesWatchlistUseCase,
    private val getFilterUseCase: GetWatchlistFilterUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ListsWatchlistDestination>()

    private val initialState = AllWatchlistState()

    private val isHomeWatchlist = MutableStateFlow(destination.homeWatchlist)
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null

    init {
        loadBackground()
        loadData()
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    fun loadData(
        ignoreErrors: Boolean = false,
        localOnly: Boolean = false,
    ) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val filter = when {
                    !destination.homeWatchlist -> loadFilter()
                    else -> null
                }

                val localItems = when {
                    destination.homeWatchlist -> {
                        getHomeWatchlistUseCase.getLocalWatchlist()
                    }
                    else -> when (filter) {
                        MEDIA -> getWatchlistUseCase.getLocalWatchlist()
                        SHOWS -> getShowsWatchlistUseCase.getLocalWatchlist()
                        MOVIES -> getMoviesWatchlistUseCase.getLocalWatchlist()
                        else -> emptyList<WatchlistItem>().toImmutableList()
                    }
                }

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                    if (localOnly) {
                        return@launch
                    }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    when {
                        destination.homeWatchlist -> {
                            getHomeWatchlistUseCase.getWatchlist()
                        }
                        else -> when (filter) {
                            MEDIA -> getWatchlistUseCase.getWatchlist()
                            SHOWS -> getShowsWatchlistUseCase.getWatchlist()
                            MOVIES -> getMoviesWatchlistUseCase.getWatchlist()
                            else -> emptyList<WatchlistItem>().toImmutableList()
                        }
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.w(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadFilter(): WatchlistFilter {
        val filter = getFilterUseCase.getFilter()
        filterState.update { filter }
        return filter
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<WatchlistItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        }

        return false
    }

    fun setFilter(newFilter: WatchlistFilter) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            getFilterUseCase.setFilter(newFilter)
            loadFilter()
            loadData(localOnly = true)
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<AllWatchlistState> = combine(
        loadingState,
        itemsState,
        filterState,
        errorState,
        backgroundState,
        isHomeWatchlist,
    ) { state ->
        AllWatchlistState(
            loading = state[0] as LoadingState,
            items = state[1] as? ImmutableList<WatchlistItem>,
            filter = state[2] as? WatchlistFilter,
            error = state[3] as? Exception,
            backgroundUrl = state[4] as? String,
            isHomeWatchlist = state[5] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
