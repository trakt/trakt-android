package tv.trakt.trakt.core.lists.features.smart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode
import tv.trakt.trakt.common.model.lists.ParentalGuideCategory
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.lists.features.smart.navigation.CreateSmartListDestination
import tv.trakt.trakt.core.lists.features.smart.usecase.CreateSmartListUseCase
import tv.trakt.trakt.core.lists.features.smart.usecase.GetSmartListPreviewUseCase
import tv.trakt.trakt.core.lists.features.smart.usecase.UpdateSmartListUseCase
import tv.trakt.trakt.core.lists.model.SmartListItem

@Suppress("UNCHECKED_CAST")
internal class CreateSmartListViewModel(
    savedStateHandle: SavedStateHandle,
    private val getSmartListPreviewUseCase: GetSmartListPreviewUseCase,
    private val createSmartListUseCase: CreateSmartListUseCase,
    private val updateSmartListUseCase: UpdateSmartListUseCase,
    private val collectionStateProvider: CollectionStateProvider,
    private val filterManager: GlobalFilterManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<CreateSmartListDestination>()
    private val editList = destination.listJson?.let { Json.decodeFromString<SmartList>(it) }
    private val initialFilters = editList?.filters
        ?: destination.filtersJson?.let { Json.decodeFromString<SmartListFilters>(it) }
        ?: SmartListFilters.Default

    private val initialState = CreateSmartListState(
        editList = editList,
        initialFilters = initialFilters,
        filters = initialFilters,
        parentalGuideExpanded = filterManager.isParentalGuideExpanded(),
    )

    private val itemsState = MutableStateFlow(initialState.items)
    private val filtersState = MutableStateFlow(initialState.filters)
    private val creatingState = MutableStateFlow(initialState.creating)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val successState = MutableStateFlow(initialState.success)
    private val parentalGuideExpandedState = MutableStateFlow(initialState.parentalGuideExpanded)

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

    fun saveList(name: String) {
        if (creatingState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            creatingState.update { Loading }
            errorState.update { null }

            try {
                when (editList) {
                    null -> createList(name)
                    else -> updateList(editList, name)
                }
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

    private suspend fun createList(name: String) {
        createSmartListUseCase.createList(
            name = name,
            filters = filtersState.value,
        )
        analytics.lists.logSmartListCreate()
    }

    private suspend fun updateList(
        list: SmartList,
        name: String,
    ) {
        updateSmartListUseCase.updateList(
            listId = list.ids.trakt,
            name = name,
            filters = filtersState.value,
        )
        analytics.lists.logSmartListUpdate()
    }

    fun setFilters(newFilters: SmartListFilters) {
        if (newFilters == filtersState.value) {
            return
        }
        logParentalChanges(previous = filtersState.value, current = newFilters)
        filtersState.update { newFilters }
        loadData()
    }

    private fun logParentalChanges(
        previous: SmartListFilters,
        current: SmartListFilters,
    ) {
        ParentalGuideCategory.entries
            .filter { previous.parentalRange(it) != current.parentalRange(it) }
            .forEach { category ->
                analytics.logFilter(
                    id = category.apiKey,
                    set = current.parentalRange(category) != null,
                    mode = GlobalFilterMode.Advanced.name,
                )
            }
    }

    fun setParentalGuideExpanded(expanded: Boolean) {
        parentalGuideExpandedState.update { expanded }
        viewModelScope.launch {
            filterManager.setParentalGuideExpanded(expanded)
        }
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
        parentalGuideExpandedState,
    ) { state ->
        CreateSmartListState(
            editList = editList,
            initialFilters = initialFilters,
            items = state[0] as? ImmutableList<SmartListItem>,
            filters = state[1] as SmartListFilters,
            creating = state[2] as LoadingState,
            loading = state[3] as LoadingState,
            error = state[4] as? Exception,
            success = state[5] as Boolean,
            collection = state[6] as UserCollectionState,
            parentalGuideExpanded = state[7] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
