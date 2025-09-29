package tv.trakt.trakt.core.lists.sections.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.core.lists.model.ListsMediaFilter
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.MEDIA
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.MOVIES
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.SHOWS
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.data.AllWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetMoviesWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.filters.GetWatchlistFilterUseCase
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource

internal class ListsWatchlistViewModel(
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val getShowsWatchlistUseCase: GetShowsWatchlistUseCase,
    private val getMoviesWatchlistUseCase: GetMoviesWatchlistUseCase,
    private val getFilterUseCase: GetWatchlistFilterUseCase,
    private val userWatchlistSource: UserWatchlistLocalDataSource,
    private val allWatchlistSource: AllWatchlistLocalDataSource,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = ListsWatchlistState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null

    init {
        loadData()
        observeUser()
        observeWatchlist()
    }

    @OptIn(FlowPreview::class)
    private fun observeUser() {
        viewModelScope.launch {
            userState.update { sessionManager.getProfile() }
            sessionManager.observeProfile()
                .drop(1)
                .distinctUntilChanged()
                .debounce(250)
                .collect { user ->
                    userState.update { user }
                    loadData()
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeWatchlist() {
        merge(
            userWatchlistSource.observeUpdates(),
            allWatchlistSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(250)
            .onEach {
                loadData(ignoreErrors = true)
            }.launchIn(viewModelScope)
    }

    fun loadData(ignoreErrors: Boolean = false) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val filter = loadFilter()
                val localItems = when (filter) {
                    MEDIA -> getWatchlistUseCase.getLocalWatchlist(LISTS_SECTION_LIMIT)
                    SHOWS -> getShowsWatchlistUseCase.getLocalWatchlist(
                        LISTS_SECTION_LIMIT,
                    )
                    MOVIES -> getMoviesWatchlistUseCase.getLocalWatchlist(
                        LISTS_SECTION_LIMIT,
                    )
                }

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    when (filter) {
                        MEDIA -> getWatchlistUseCase.getWatchlist(LISTS_SECTION_LIMIT)
                        SHOWS -> getShowsWatchlistUseCase.getWatchlist(
                            LISTS_SECTION_LIMIT,
                        )
                        MOVIES -> getMoviesWatchlistUseCase.getWatchlist(
                            LISTS_SECTION_LIMIT,
                        )
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

    private suspend fun loadFilter(): ListsMediaFilter {
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

    fun setFilter(newFilter: ListsMediaFilter) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            getFilterUseCase.setFilter(newFilter)
            loadFilter()
            loadData()
        }
    }

    val state: StateFlow<ListsWatchlistState> = combine(
        loadingState,
        itemsState,
        filterState,
        errorState,
        userState,
    ) { s0, s1, s2, s3, s4 ->
        ListsWatchlistState(
            loading = s0,
            items = s1,
            filter = s2,
            error = s3,
            user = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
