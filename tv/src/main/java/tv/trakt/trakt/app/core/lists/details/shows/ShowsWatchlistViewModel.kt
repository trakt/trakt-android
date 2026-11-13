package tv.trakt.trakt.app.core.lists.details.shows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.usecases.GetListsShowsWatchlistUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.analytics.logListSort
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.sorting.Sorting

@Suppress("UNCHECKED_CAST")
internal class ShowsWatchlistViewModel(
    collectionStateProvider: CollectionStateProvider,
    private val getListItemsUseCase: GetListsShowsWatchlistUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ShowsWatchlistState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val showsState = MutableStateFlow(initialState.shows)
    private val errorState = MutableStateFlow(initialState.error)
    private val sortingState = MutableStateFlow(initialState.sorting)
    private val itemsSortingState = MutableStateFlow(initialState.itemsSorting)

    private var nextDataPage: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadData()
        collectionStateProvider.launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }
                val shows = getListItemsUseCase.getShows(
                    limit = LISTS_PAGE_LIMIT,
                    sorting = sortingState.value,
                )
                showsState.update { shows }
                itemsSortingState.update { sortingState.value }
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun loadNextDataPage() {
        if (loadingPageState.value || !hasMoreData) {
            return
        }
        viewModelScope.launch {
            try {
                loadingPageState.update { true }

                val shows = getListItemsUseCase.getShows(
                    limit = LISTS_PAGE_LIMIT,
                    sorting = sortingState.value,
                    page = nextDataPage,
                )

                showsState.update { current ->
                    current
                        ?.toPersistentList()
                        ?.plus(shows)
                        ?.distinctBy { it.id }
                        ?.toImmutableList()
                }

                hasMoreData = (shows.size >= LISTS_PAGE_LIMIT)
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingPageState.update { false }
            }
        }
    }

    fun setSorting(sorting: Sorting) {
        if (sorting == sortingState.value || loadingState.value || loadingPageState.value) {
            return
        }

        sortingState.update { sorting }
        nextDataPage = 1
        hasMoreData = true
        loadData()

        analytics.lists.logListSort(sorting)
    }

    val state = combine(
        loadingState,
        loadingPageState,
        showsState,
        collectionStateProvider.stateFlow.map { collection ->
            collection
                .watchlistCount(MediaMode.Shows)
                .takeIf { it > 0 }
        },
        errorState,
        sortingState,
        itemsSortingState,
    ) { state ->
        ShowsWatchlistState(
            isLoading = state[0] as Boolean,
            isLoadingPage = state[1] as Boolean,
            shows = state[2] as ImmutableList<ListMediaItem.ShowItem>?,
            itemCount = state[3] as Int?,
            error = state[4] as Exception?,
            sorting = state[5] as Sorting,
            itemsSorting = state[6] as Sorting,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
