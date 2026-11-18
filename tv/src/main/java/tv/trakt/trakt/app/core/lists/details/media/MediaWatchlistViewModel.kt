package tv.trakt.trakt.app.core.lists.details.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListState
import tv.trakt.trakt.app.core.lists.usecases.GetListsMediaWatchlistUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.analytics.logListSort
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.sorting.Sorting

@Suppress("UNCHECKED_CAST")
internal class MediaWatchlistViewModel(
    private val getListItemsUseCase: GetListsMediaWatchlistUseCase,
    private val collectionStateProvider: CollectionStateProvider,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = PersonalListState(sorting = Sorting.RecentlyAdded)

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val itemsState = MutableStateFlow(initialState.items)
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
                val items = getListItemsUseCase.getItems(
                    limit = PERSONAL_LIST_PAGE_LIMIT,
                    sorting = sortingState.value,
                )
                itemsState.update { items }
                itemsSortingState.update { sortingState.value }
                hasMoreData = items.size >= PERSONAL_LIST_PAGE_LIMIT
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

                val items = getListItemsUseCase.getItems(
                    limit = PERSONAL_LIST_PAGE_LIMIT,
                    sorting = sortingState.value,
                    page = nextDataPage,
                )

                itemsState.update { current ->
                    current
                        ?.toPersistentList()
                        ?.plus(items)
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                hasMoreData = items.size >= PERSONAL_LIST_PAGE_LIMIT
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
        itemsState,
        collectionStateProvider.stateFlow,
        errorState,
        sortingState,
        itemsSortingState,
    ) { state ->
        PersonalListState(
            isLoading = state[0] as Boolean,
            isLoadingPage = state[1] as Boolean,
            items = state[2] as ImmutableList<ListMediaItem>?,
            collection = state[3] as UserCollectionState,
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
