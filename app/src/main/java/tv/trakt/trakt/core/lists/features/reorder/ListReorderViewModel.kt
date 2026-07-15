package tv.trakt.trakt.core.lists.features.reorder

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
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.MediaType.Episode
import tv.trakt.trakt.common.model.MediaType.Movie
import tv.trakt.trakt.common.model.MediaType.Season
import tv.trakt.trakt.common.model.MediaType.Show
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ITEMS_ALL_LIMIT
import tv.trakt.trakt.core.lists.features.details.usecases.GetListItemsUseCase
import tv.trakt.trakt.core.lists.features.reorder.data.ReorderUpdates
import tv.trakt.trakt.core.lists.features.reorder.navigation.ListReorderDestination
import tv.trakt.trakt.core.lists.features.reorder.usecase.ReorderListUseCase
import tv.trakt.trakt.core.lists.model.CustomListItem

@Suppress("UNCHECKED_CAST")
internal class ListReorderViewModel(
    savedStateHandle: SavedStateHandle,
    private val getListItemsUseCase: GetListItemsUseCase,
    private val reorderListUseCase: ReorderListUseCase,
    private val reorderUpdates: ReorderUpdates,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ListReorderDestination>()
    private val destinationList = Json.decodeFromString<CustomList>(destination.listJson)

    private val initialState = ListReorderState()

    private val listState = MutableStateFlow(destinationList)
    private val itemsState = MutableStateFlow(initialState.items)
    private val initialItemsOrderState = MutableStateFlow(initialState.initialItemsOrder)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val doneState = MutableStateFlow(initialState.done)

    private var dataJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                itemsState.update { EmptyImmutableList }
                loadingState.update { Done }
                return@launch
            }

            try {
                loadingState.update { Loading }

                val all = mutableListOf<CustomListItem>()
                var page = 1

                while (true) {
                    val pageItems = getListItemsUseCase.getItems(
                        listId = destinationList.ids.trakt,
                        type = listOf(Show, Movie, Season, Episode),
                        sorting = Sorting.Default,
                        pagination = Pagination(page, LISTS_ITEMS_ALL_LIMIT),
                        filters = null,
                    )

                    all += pageItems

                    if (pageItems.size < LISTS_ITEMS_ALL_LIMIT) break
                    page += 1
                }

                val allItems = all
                    .distinctBy { it.key }
                    .toImmutableList()

                itemsState.update {
                    allItems
                }

                initialItemsOrderState.update {
                    allItems
                        .map { it.itemId }
                        .toImmutableList()
                }
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

    fun reorderItem(
        from: Int,
        to: Int,
    ) {
        itemsState.update { current ->
            if (current == null) return@update current
            if (from !in current.indices || to !in current.indices) return@update current
            if (from == to) return@update current

            current.toMutableList()
                .apply { add(to, removeAt(from)) }
                .toImmutableList()
        }
    }

    fun moveToTop(index: Int) {
        reorderItem(from = index, to = 0)
    }

    fun moveToBottom(index: Int) {
        val lastIndex = itemsState.value?.lastIndex ?: return
        reorderItem(from = index, to = lastIndex)
    }

    fun moveToPosition(
        index: Int,
        position: Int,
    ) {
        val size = itemsState.value?.size ?: return
        reorderItem(from = index, to = position.coerceIn(1, size) - 1)
    }

    fun applyChanges() {
        if (loadingState.value.isLoading) return

        val currentItems = itemsState.value ?: return
        val initialItemsOrder = initialItemsOrderState.value ?: return

        if (currentItems.map { it.itemId } == initialItemsOrder) {
            return
        }

        viewModelScope.launch {
            val listId = destinationList.ids.trakt
            try {
                loadingState.update { Loading }

                reorderListUseCase.reorderList(
                    listId = listId,
                    itemIds = currentItems.map { it.itemId },
                )

                reorderUpdates.notifyUpdate(listId)
                doneState.update { true }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    loadingState.update { Idle }
                    Timber.recordError(error)
                }
            }
        }
    }

    fun clearError() {
        errorState.update { null }
    }

    override fun onCleared() {
        dataJob?.cancel()
        super.onCleared()
    }

    val state = combine(
        loadingState,
        listState,
        itemsState,
        initialItemsOrderState,
        errorState,
        doneState,
    ) { state ->
        ListReorderState(
            loading = state[0] as LoadingState,
            list = state[1] as CustomList?,
            items = state[2] as ImmutableList<CustomListItem>?,
            initialItemsOrder = state[3] as ImmutableList<Int>?,
            error = state[4] as Exception?,
            done = state[5] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
