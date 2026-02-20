package tv.trakt.trakt.core.lists.features.details

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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ALL_LIMIT
import tv.trakt.trakt.core.lists.features.details.ListDetailsState.ListDetailsInfo
import tv.trakt.trakt.core.lists.features.details.navigation.ListsDetailsDestination
import tv.trakt.trakt.core.lists.features.details.usecases.GetListItemsUseCase
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.core.user.usecases.lists.LoadUserLikedListsUseCase

private const val PAGE_LIMIT = LISTS_ALL_LIMIT

@OptIn(FlowPreview::class)
internal class ListDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getListItemsUseCase: GetListItemsUseCase,
    private val getListLikedUseCase: LoadUserLikedListsUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val collectionStateProvider: CollectionStateProvider,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ListsDetailsDestination>()
    private val showFilters = destination.mediaType.size > 1

    private val initialState = ListDetailsState()

    private val listState = MutableStateFlow(
        ListDetailsInfo(
            name = destination.listTitle,
            description = destination.listDescription,
            mediaId = destination.mediaId.toTraktId(),
        ),
    )
    private val itemsState = MutableStateFlow(initialState.items)
    private val likedState = MutableStateFlow(initialState.liked)
    private val filterState = MutableStateFlow(if (showFilters) MediaMode.MEDIA else null)
    private val sortingState = MutableStateFlow(initialState.sorting)
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
        loadData()
        loadLikedData()

        observeCollection()
    }

    private fun loadLikedData() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) return@launch
            try {
                val likedLists = when {
                    getListLikedUseCase.isLoaded() -> getListLikedUseCase.loadLocalLists()
                    else -> getListLikedUseCase.loadLists()
                }

                likedState.update {
                    likedLists.containsKey(destination.listId.toTraktId())
                }
            } catch (error: Exception) {
                Timber.recordError(error)
            }
        }
    }

    fun loadData(
        ignoreErrors: Boolean = false,
        ignoreLoading: Boolean = false,
    ) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                page = 1
                hasMoreData = true

                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                if (!ignoreLoading) {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getListItemsUseCase.getItems(
                        listId = destination.listId.toTraktId(),
                        type = filterState.value.toMediaTypes(),
                        sorting = sortingState.value,
                        pagination = Pagination(1, PAGE_LIMIT),
                    )
                        .distinctBy { it.key }
                        .toImmutableList()
                }

                hasMoreData = (itemsState.value?.size ?: 0) >= PAGE_LIMIT
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

    private fun observeCollection() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<CustomListItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        }

        return false
    }

    fun loadMoreData() {
        if (loadingState.value.isLoading ||
            loadingMoreState.value.isLoading ||
            dataJob?.isActive == true ||
            !hasMoreData ||
            (itemsState.value?.size ?: 0) < PAGE_LIMIT
        ) {
            return
        }

        dataJob = viewModelScope.launch {
            try {
                loadingMoreState.update { LOADING }
                val newItems = getListItemsUseCase.getItems(
                    listId = destination.listId.toTraktId(),
                    type = filterState.value.toMediaTypes(),
                    sorting = sortingState.value,
                    pagination = Pagination(
                        page + 1,
                        PAGE_LIMIT,
                    ),
                )

                itemsState.update { items ->
                    items
                        ?.plus(newItems)
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                page += 1
                hasMoreData = (newItems.size >= PAGE_LIMIT)
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

    fun setSorting(newSorting: Sorting) {
        if (newSorting == sortingState.value || loadingState.value.isLoading) {
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

    fun setFilter(newFilter: MediaMode) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }

        filterState.update { newFilter }
        loadData()
    }

    private fun MediaMode?.toMediaTypes(): List<MediaType> {
        return when (this) {
            MediaMode.SHOWS -> listOf(MediaType.SHOW)
            MediaMode.MOVIES -> listOf(MediaType.MOVIE)
            else -> destination.mediaType.map { MediaType.valueOf(it) }
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
        listState,
        itemsState,
        filterState,
        sortingState,
        likedState,
        collectionStateProvider.stateFlow,
        navigateShow,
        navigateMovie,
        errorState,
    ) { state ->
        ListDetailsState(
            loading = state[0] as LoadingState,
            loadingMore = state[1] as LoadingState,
            list = state[2] as ListDetailsInfo,
            items = state[3] as ImmutableList<CustomListItem>?,
            filter = state[4] as MediaMode?,
            sorting = state[5] as Sorting,
            liked = state[6] as Boolean?,
            collection = state[7] as UserCollectionState,
            navigateShow = state[8] as TraktId?,
            navigateMovie = state[9] as TraktId?,
            error = state[10] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
