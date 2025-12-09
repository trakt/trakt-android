package tv.trakt.trakt.core.lists.sections.personal.features.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ALL_LIMIT
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.sections.personal.features.all.navigation.ListsPersonalDestination
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.core.user.data.local.UserListsLocalDataSource

@OptIn(FlowPreview::class)
internal class AllPersonalListViewModel(
    savedStateHandle: SavedStateHandle,
    modeManager: MediaModeManager,
    private val getListUseCase: GetPersonalListsUseCase,
    private val getListItemsUseCase: GetPersonalListItemsUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val userListLocalDataSource: UserListsLocalDataSource,
    private val collectionStateProvider: CollectionStateProvider,
    private val sessionManager: SessionManager,
    analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ListsPersonalDestination>()

    private val initialState = AllPersonalListState()

    private val filterState = MutableStateFlow(modeManager.getMode())
    private val sortingState = MutableStateFlow(initialState.sorting)
    private val listState = MutableStateFlow(initialState.list)
    private val itemsState = MutableStateFlow(initialState.items)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    private var page: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadDetails()
        loadData()

        observeLists()
        observeCollection()

        analytics.logScreenView(
            screenName = "all_personal_list",
        )
    }

    private fun observeLists() {
        viewModelScope.launch {
            merge(
                userListLocalDataSource.observeUpdates(),
            )
                .distinctUntilChanged()
                .debounce(200)
                .collect {
                    loadData(
                        ignoreErrors = true,
                        ignoreLoading = true,
                    )
                }
        }
    }

    private fun observeCollection() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    fun loadDetails() {
        viewModelScope.launch {
            listState.update {
                getListUseCase.getLocalList(destination.listId.toTraktId())
            }
        }
    }

    private fun loadData(
        ignoreErrors: Boolean = false,
        ignoreLoading: Boolean = false,
    ) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                page = 1

                if (loadEmptyIfNeeded()) {
                    loadingState.update { DONE }
                    return@launch
                }

                if (!ignoreLoading) {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getListItemsUseCase.getRemoteItems(
                        listId = destination.listId.toTraktId(),
                        type = filterState.value,
                        sorting = sortingState.value,
                        page = 1,
                        limit = LISTS_ALL_LIMIT,
                    )
                        .distinctBy { it.key }
                        .toImmutableList()
                }

                hasMoreData = (itemsState.value?.size ?: 0) >= LISTS_ALL_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
                dataJob = null
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { EmptyImmutableList }
            return true
        }

        return false
    }

    fun loadMoreData() {
        if (loadingState.value.isLoading ||
            loadingMoreState.value.isLoading ||
            dataJob?.isActive == true ||
            !hasMoreData ||
            itemsState.value.isNullOrEmpty()
        ) {
            return
        }

        dataJob = viewModelScope.launch {
            try {
                loadingMoreState.update { LOADING }
                val newItems = getListItemsUseCase.getRemoteItems(
                    listId = destination.listId.toTraktId(),
                    type = filterState.value,
                    sorting = sortingState.value,
                    page = page + 1,
                    limit = LISTS_ALL_LIMIT,
                )

                itemsState.update {
                    it?.plus(newItems)?.toImmutableList()
                }

                page += 1
                hasMoreData = (newItems.size >= LISTS_ALL_LIMIT)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingMoreState.update { DONE }
                dataJob = null
            }
        }
    }

    fun setFilter(newFilter: MediaMode) {
        if (newFilter == filterState.value ||
            loadingState.value.isLoading ||
            loadingMoreState.value.isLoading
        ) {
            return
        }
        viewModelScope.launch {
            filterState.update { newFilter }
            loadData()
        }
    }

    fun setSorting(newSorting: Sorting) {
        if (newSorting == sortingState.value ||
            loadingState.value.isLoading ||
            loadingMoreState.value.isLoading
        ) {
            return
        }
        viewModelScope.launch {
            sortingState.update {
                it.copy(
                    type = newSorting.type,
                    order = newSorting.order,
                )
            }
            loadData()
        }
    }

    fun removeItem(item: PersonalListItem?) {
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

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
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
        filterState,
        sortingState,
        listState,
        itemsState,
        collectionStateProvider.stateFlow,
        navigateShow,
        navigateMovie,
        errorState,
    ) { state ->
        AllPersonalListState(
            loading = state[0] as LoadingState,
            loadingMore = state[1] as LoadingState,
            filter = state[2] as MediaMode?,
            sorting = state[3] as Sorting,
            list = state[4] as? CustomList,
            items = state[5] as? ImmutableList<PersonalListItem>,
            collection = state[6] as UserCollectionState,
            navigateShow = state[7] as? TraktId,
            navigateMovie = state[8] as? TraktId,
            error = state[9] as? Exception,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
