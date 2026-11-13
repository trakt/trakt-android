package tv.trakt.trakt.app.core.lists.details.personal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem
import tv.trakt.trakt.app.core.lists.details.personal.navigation.PersonalListDestination
import tv.trakt.trakt.app.core.lists.details.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.analytics.logListSort
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId

@Suppress("UNCHECKED_CAST")
internal class PersonalListViewModel(
    savedStateHandle: SavedStateHandle,
    private val getListItemsUseCase: GetPersonalListItemsUseCase,
    private val collectionStateProvider: CollectionStateProvider,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = PersonalListState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

    val destination = savedStateHandle.toRoute<PersonalListDestination>()

    private val sortingState = MutableStateFlow(
        Sorting.fromValues(
            sortBy = destination.sortBy,
            sortHow = destination.sortHow,
        ) ?: initialState.sorting,
    )
    private val itemsSortingState = MutableStateFlow(sortingState.value)

    private var nextDataPage: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadData()

        observeData()
    }

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }
                val items = getListItemsUseCase.getListItems(
                    listId = destination.listId.toTraktId(),
                    sorting = sortingState.value,
                    page = nextDataPage,
                )
                itemsState.update { items }
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

                val shows = getListItemsUseCase.getListItems(
                    listId = destination.listId.toTraktId(),
                    sorting = sortingState.value,
                    page = nextDataPage,
                )

                itemsState.update { current ->
                    current
                        ?.toPersistentList()
                        ?.plus(shows)
                        ?.distinctBy { it.id }
                        ?.toImmutableList()
                }

                hasMoreData = (shows.size >= PERSONAL_LIST_PAGE_LIMIT)
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
            items = state[2] as ImmutableList<PersonalListItem>?,
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
