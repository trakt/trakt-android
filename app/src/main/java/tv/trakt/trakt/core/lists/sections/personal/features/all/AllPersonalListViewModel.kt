package tv.trakt.trakt.core.lists.sections.personal.features.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ITEMS_ALL_LIMIT
import tv.trakt.trakt.core.lists.features.reorder.data.ReorderUpdates
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.sections.personal.features.all.navigation.ListsPersonalDestination
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
internal class AllPersonalListViewModel(
    savedStateHandle: SavedStateHandle,
    filterManager: GlobalFilterManager,
    analytics: Analytics,
    private val getListUseCase: GetPersonalListsUseCase,
    private val getListItemsUseCase: GetPersonalListItemsUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val collectionStateProvider: CollectionStateProvider,
    private val reorderUpdates: ReorderUpdates,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ListsPersonalDestination>()

    private val initialState = AllPersonalListState()

    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val userState = MutableStateFlow(initialState.user)
    private val sortingState = MutableStateFlow(initialState.sorting)
    private val listState = MutableStateFlow(initialState.list)
    private val itemsState = MutableStateFlow(initialState.items)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null
    private var loadingJob: Job? = null

    private var page: Int = 1
    private var hasMoreData: Boolean = false

    init {
        loadDetails()
        loadInitialData()
        loadUser()

        observeCollection()
        observeReorder()

        analytics.logScreenView(
            screenName = "all_personal_list",
        )
    }

    private fun observeCollection() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun observeReorder() {
        reorderUpdates.observeUpdates(destination.listId.toTraktId())
            .distinctUntilChanged()
            .debounce(250.milliseconds)
            .onEach { loadData(ignoreErrors = true) }
            .launchIn(viewModelScope)
    }

    fun loadDetails() {
        viewModelScope.launch {
            listState.update {
                getListUseCase.getLocalList(destination.listId.toTraktId())
            }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            userState.update {
                sessionManager.getProfile()
            }
        }
    }

    private fun loadInitialData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) return@launch
                loadingIfNeeded()

                val items = getListItemsUseCase.getRemoteItems(
                    listId = destination.listId.toTraktId(),
                    page = 1,
                    limit = LISTS_ITEMS_ALL_LIMIT,
                    filter = filterState.value,
                    sorting = sortingState.value,
                )
                    .distinctBy { it.key }
                    .toImmutableList()

                itemsState
                    .update { items }
                    .also {
                        hasMoreData = items.size >= LISTS_ITEMS_ALL_LIMIT
                    }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null

                loadingJob?.cancel()
                loadingJob = null
            }
        }
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }

                page = 1
                hasMoreData = false

                val items = getListItemsUseCase.getRemoteItems(
                    listId = destination.listId.toTraktId(),
                    page = 1,
                    limit = LISTS_ITEMS_ALL_LIMIT,
                    sorting = sortingState.value,
                    filter = filterState.value,
                )
                    .distinctBy { it.key }
                    .toImmutableList()

                itemsState
                    .update { items }
                    .also {
                        hasMoreData = items.size >= LISTS_ITEMS_ALL_LIMIT
                    }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null

                loadingJob?.cancel()
                loadingJob = null
            }
        }
    }

    fun loadMoreData() {
        if (itemsState.value.isNullOrEmpty() || !hasMoreData) {
            return
        }
        if (loadingMoreState.value.isLoading || loadingState.value.isLoading) {
            return
        }

        dataJob = viewModelScope.launch {
            try {
                loadingMoreState.update { Loading }

                val nextPage = page + 1
                val nextItems = getListItemsUseCase.getRemoteItems(
                    listId = destination.listId.toTraktId(),
                    filter = filterState.value,
                    sorting = sortingState.value,
                    page = nextPage,
                    limit = LISTS_ITEMS_ALL_LIMIT,
                )

                itemsState.update { items ->
                    items
                        ?.plus(nextItems)
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                page = nextPage
                hasMoreData = (nextItems.size >= LISTS_ITEMS_ALL_LIMIT)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingMoreState.update { Done }
                dataJob = null
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { EmptyImmutableList }
            loadingState.update { Done }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { Idle }
        }

        return false
    }

    private fun CoroutineScope.loadingIfNeeded() {
        loadingJob = launch {
            if (itemsState.value?.isNotEmpty() == true) {
                // Avoid blinking loading but still show it if loading takes too long
                delay(100.milliseconds)
            }
            loadingState.update { Loading }
        }
    }

    fun setFilter(newFilter: GlobalFilter) {
        if (newFilter == filterState.value) {
            return
        }
        filterState.update { newFilter }
        loadData()
    }

    fun setSorting(newSorting: Sorting) {
        if (newSorting == sortingState.value ||
            loadingState.value.isLoading ||
            loadingMoreState.value.isLoading
        ) {
            return
        }

        sortingState.update {
            it.copy(
                type = newSorting.type,
                order = newSorting.order,
            )
        }

        loadData()
    }

    fun removeItem(item: CustomListItem?) {
        val currentItems = itemsState.value ?: return
        itemsState.update {
            currentItems
                .filterNot { it.key == item?.key }
                .toImmutableList()
        }
    }

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShow.update { show.ids.trakt }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            movieLocalDataSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie.ids.trakt }
        }
    }

    fun navigateToEpisode(
        show: Show,
        episode: Episode,
    ) {
        if (navigateEpisode.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            episodeLocalDataSource.upsertEpisodes(listOf(episode))

            navigateEpisode.update {
                Pair(show.ids.trakt, episode)
            }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
        navigateEpisode.update { null }
    }

    override fun onCleared() {
        dataJob?.cancel()
        processingJob?.cancel()
        super.onCleared()
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        loadingState,
        loadingMoreState,
        userState,
        filterState,
        sortingState,
        listState,
        itemsState,
        collectionStateProvider.stateFlow,
        navigateShow,
        navigateMovie,
        navigateEpisode,
        errorState,
    ) { state ->
        AllPersonalListState(
            loading = state[0] as LoadingState,
            loadingMore = state[1] as LoadingState,
            user = state[2] as User?,
            filter = state[3] as GlobalFilter?,
            sorting = state[4] as Sorting,
            list = state[5] as? CustomList,
            items = state[6] as? ImmutableList<CustomListItem>,
            collection = state[7] as UserCollectionState,
            navigateShow = state[8] as? TraktId,
            navigateMovie = state[9] as? TraktId,
            navigateEpisode = state[10] as? Pair<TraktId, Episode>,
            error = state[11] as? Exception,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
