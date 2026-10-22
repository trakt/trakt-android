package tv.trakt.trakt.core.lists.features.reorder.watchlist

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
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.lists.features.reorder.watchlist.usecase.ReorderWatchlistUseCase

@Suppress("UNCHECKED_CAST")
internal class WatchlistReorderViewModel(
    private val reorderWatchlistUseCase: ReorderWatchlistUseCase,
    private val dataChanges: DataChanges,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = WatchlistReorderState()

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

                val allItems = reorderWatchlistUseCase.getRankedItems()

                itemsState.update { allItems }
                initialItemsOrderState.update {
                    allItems
                        .map { it.id }
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

        val currentItems = itemsState.value ?: return
        val initialItemsOrder = initialItemsOrderState.value ?: return

        val itemIds = currentItems.map { it.id }
        if (itemIds == initialItemsOrder) {
            return
        }

        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                reorderWatchlistUseCase.reorderWatchlist(itemIds)

                dataChanges.notify(Kind.Watchlist)
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
        itemsState,
        initialItemsOrderState,
        errorState,
        doneState,
    ) { state ->
        WatchlistReorderState(
            loading = state[0] as LoadingState,
            items = state[1] as ImmutableList<WatchlistReorderEntry>?,
            initialItemsOrder = state[2] as ImmutableList<Int>?,
            error = state[3] as Exception?,
            done = state[4] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
