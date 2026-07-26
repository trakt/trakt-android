package tv.trakt.trakt.app.core.lists.details.personal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.details.personal.navigation.PersonalListDestination
import tv.trakt.trakt.app.core.lists.details.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.app.core.lists.filters.TvListFilterConfiguration
import tv.trakt.trakt.app.core.lists.filters.TvListRequest
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId

internal class PersonalListViewModel(
    savedStateHandle: SavedStateHandle,
    private val getListItemsUseCase: GetPersonalListItemsUseCase,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    val destination = savedStateHandle.toRoute<PersonalListDestination>()
    val filterConfiguration = TvListFilterConfiguration.MixedList

    private val _state = MutableStateFlow(PersonalListState())
    val state = _state.asStateFlow()

    private var requestJob: Job? = null
    private var nextDataPage = 1
    private var hasMoreData = true

    init {
        observeCollection()
        reload()
    }

    fun applyFilter(filter: GlobalFilter) {
        _state.update {
            it.copy(filter = filterConfiguration.normalize(filter))
        }
        reload()
    }

    fun applySorting(sorting: Sorting) {
        _state.update {
            it.copy(sorting = sorting)
        }
        reload()
    }

    private fun observeCollection() {
        collectionStateProvider.launchIn(viewModelScope)
        collectionStateProvider.stateFlow
            .onEach { collection ->
                _state.update { it.copy(collection = collection) }
            }
            .launchIn(viewModelScope)
    }

    private fun reload() {
        requestJob?.cancel()
        nextDataPage = 1
        hasMoreData = true
        _state.update {
            it.copy(
                isLoading = true,
                isLoadingPage = false,
                error = null,
            )
        }

        val request = _state.value.toRequest(page = 1)
        requestJob = viewModelScope.launch {
            try {
                val page = getListItemsUseCase.getListItems(
                    listId = destination.listId.toTraktId(),
                    request = request,
                )
                nextDataPage = page.nextPage
                hasMoreData = page.hasMore
                _state.update {
                    it.copy(
                        isLoading = false,
                        items = page.items,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error,
                        )
                    }
                }
            }
        }
    }

    fun loadNextDataPage() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isLoadingPage || !hasMoreData) return

        _state.update {
            it.copy(
                isLoadingPage = true,
                error = null,
            )
        }
        val request = _state.value.toRequest(page = nextDataPage)
        requestJob = viewModelScope.launch {
            try {
                val page = getListItemsUseCase.getListItems(
                    listId = destination.listId.toTraktId(),
                    request = request,
                )
                nextDataPage = page.nextPage
                hasMoreData = page.hasMore
                _state.update { state ->
                    state.copy(
                        isLoadingPage = false,
                        items = (
                            state.items.orEmpty() + page.items
                        ).distinctBy { it.id }
                            .toImmutableList(),
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    _state.update {
                        it.copy(
                            isLoadingPage = false,
                            error = error,
                        )
                    }
                }
            }
        }
    }

    private fun PersonalListState.toRequest(page: Int): TvListRequest {
        return TvListRequest(
            page = page,
            limit = PERSONAL_LIST_PAGE_LIMIT,
            filter = filter,
            sorting = sorting,
        )
    }
}
