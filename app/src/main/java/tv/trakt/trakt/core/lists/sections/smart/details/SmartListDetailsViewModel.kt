package tv.trakt.trakt.core.lists.sections.smart.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ITEMS_ALL_LIMIT
import tv.trakt.trakt.core.lists.model.SmartListItem
import tv.trakt.trakt.core.lists.sections.smart.details.navigation.SmartListDetailsDestination
import tv.trakt.trakt.core.lists.sections.smart.usecases.DeleteSmartListUseCase
import tv.trakt.trakt.core.lists.sections.smart.usecases.GetSmartListItemsUseCase

private const val PAGE_LIMIT = LISTS_ITEMS_ALL_LIMIT

internal class SmartListDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getSmartListItemsUseCase: GetSmartListItemsUseCase,
    private val deleteSmartListUseCase: DeleteSmartListUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<SmartListDetailsDestination>()
    private val destinationList = Json.decodeFromString<SmartList>(destination.listJson)

    private val initialState = SmartListDetailsState()

    private val listState = MutableStateFlow<SmartList?>(destinationList)
    private val itemsState = MutableStateFlow(initialState.items)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val deletingState = MutableStateFlow(initialState.deleting)
    private val deletedState = MutableStateFlow(initialState.deleted)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    private var page: Int = 1
    private var hasMoreData: Boolean = false

    init {
        loadInitialData()
        observeCollection()
    }

    private fun observeCollection() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    fun loadInitialData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }

                page = 1

                val items = getSmartListItemsUseCase.getItems(
                    listId = destinationList.ids.trakt,
                    type = when (destinationList.mediaType) {
                        MediaMode.Media -> "all"
                        MediaMode.Shows -> "shows"
                        MediaMode.Movies -> "movies"
                    },
                    pagination = Pagination(1, PAGE_LIMIT),
                )
                    .distinctBy { it.key }
                    .toImmutableList()

                itemsState.update { items }
                hasMoreData = items.size >= PAGE_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
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
                val nextItems = getSmartListItemsUseCase.getItems(
                    listId = destinationList.ids.trakt,
                    type = when (destinationList.mediaType) {
                        MediaMode.Media -> "all"
                        MediaMode.Shows -> "shows"
                        MediaMode.Movies -> "movies"
                    },
                    pagination = Pagination(nextPage, PAGE_LIMIT),
                )

                itemsState.update { items ->
                    items
                        ?.plus(nextItems)
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                page = nextPage
                hasMoreData = (nextItems.size >= PAGE_LIMIT)
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

    fun deleteList() {
        if (deletingState.value.isLoading || deletedState.value) {
            return
        }

        viewModelScope.launch {
            try {
                deletingState.update { Loading }
                deleteSmartListUseCase.deleteList(destinationList.ids.trakt)
                deletedState.update { true }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                deletingState.update { Done }
            }
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

    fun clearError() {
        errorState.update { null }
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
        collectionStateProvider.stateFlow,
        navigateShow,
        navigateMovie,
        deletingState,
        deletedState,
        errorState,
    ) { state ->
        SmartListDetailsState(
            loading = state[0] as LoadingState,
            loadingMore = state[1] as LoadingState,
            list = state[2] as SmartList?,
            items = state[3] as ImmutableList<SmartListItem>?,
            collection = state[4] as UserCollectionState,
            navigateShow = state[5] as TraktId?,
            navigateMovie = state[6] as TraktId?,
            deleting = state[7] as LoadingState,
            deleted = state[8] as Boolean,
            error = state[9] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
