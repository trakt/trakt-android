package tv.trakt.trakt.core.lists.features.reorder.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ALL_PAGE_LIMIT
import tv.trakt.trakt.core.lists.features.reorder.lists.usecase.ReorderPersonalListsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase

@Suppress("UNCHECKED_CAST")
internal class ListsReorderViewModel(
    private val getPersonalListsUseCase: GetPersonalListsUseCase,
    private val reorderPersonalListsUseCase: ReorderPersonalListsUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = ListsReorderState()

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

                val all = mutableListOf<CustomList>()
                var page = 1

                while (true) {
                    val pageItems = getPersonalListsUseCase.getLists(
                        pagination = Pagination(page, LISTS_ALL_PAGE_LIMIT),
                    )

                    all += pageItems

                    if (pageItems.size < LISTS_ALL_PAGE_LIMIT) break
                    page += 1
                }

                val allItems = all
                    .distinctBy { it.ids.trakt }
                    .toImmutableList()

                itemsState.update { allItems }
                initialItemsOrderState.update {
                    allItems
                        .map { it.ids.trakt }
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
        val size = itemsState.value?.size?.takeIf { it > 0 } ?: return
        reorderItem(from = index, to = position.coerceIn(1, size) - 1)
    }

    fun applyChanges() {
        if (loadingState.value.isLoading) return

        val currentIds = itemsState.value?.map { it.ids.trakt } ?: return
        val initialItemsOrder = initialItemsOrderState.value ?: return

        if (currentIds == initialItemsOrder) {
            return
        }

        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                reorderPersonalListsUseCase.reorderLists(currentIds)
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

    val state = combine(
        itemsState,
        initialItemsOrderState,
        loadingState,
        errorState,
        doneState,
    ) { state ->
        ListsReorderState(
            items = state[0] as ImmutableList<CustomList>?,
            initialItemsOrder = state[1] as ImmutableList<TraktId>?,
            loading = state[2] as LoadingState,
            error = state[3] as Exception?,
            done = state[4] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
