package tv.trakt.trakt.core.lists.features.smart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.core.lists.features.smart.usecase.CreateSmartListUseCase
import tv.trakt.trakt.core.lists.features.smart.usecase.GetSmartListPreviewUseCase
import tv.trakt.trakt.core.lists.model.SmartListItem

@Suppress("UNCHECKED_CAST")
internal class CreateSmartListViewModel(
    private val getSmartListPreviewUseCase: GetSmartListPreviewUseCase,
    private val createSmartListUseCase: CreateSmartListUseCase,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val initialState = CreateSmartListState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filtersState = MutableStateFlow(initialState.filters)
    private val creatingState = MutableStateFlow(initialState.creating)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val successState = MutableStateFlow(initialState.success)

    private var dataJob: Job? = null

    init {
        loadData()
        observeCollection()
    }

    private fun observeCollection() {
        collectionStateProvider.launchIn(viewModelScope)
    }

    fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            loadingState.update { Loading }
            errorState.update { null }

            try {
                itemsState.update {
                    getSmartListPreviewUseCase.getPreviewItems(filtersState.value)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                    errorState.update { error }
                }
            } finally {
                loadingState.update { Idle }
                dataJob = null
            }
        }
    }

    fun createList(name: String) {
        if (creatingState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            creatingState.update { Loading }
            errorState.update { null }

            try {
                createSmartListUseCase.createList(
                    name = name,
                    filters = filtersState.value,
                )
                successState.update { true }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                    errorState.update { error }
                    creatingState.update { Idle }
                }
            }
        }
    }

    fun setFilters(newFilters: SmartListFilters) {
        if (newFilters == filtersState.value) {
            return
        }
        filtersState.update { newFilters }
        loadData()
    }

    fun clearError() {
        errorState.update { null }
    }

    val state = combine(
        itemsState,
        filtersState,
        creatingState,
        loadingState,
        errorState,
        successState,
        collectionStateProvider.stateFlow,
    ) { state ->
        CreateSmartListState(
            items = state[0] as? ImmutableList<SmartListItem>,
            filters = state[1] as SmartListFilters,
            creating = state[2] as LoadingState,
            loading = state[3] as LoadingState,
            error = state[4] as? Exception,
            success = state[5] as Boolean,
            collection = state[6] as UserCollectionState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
