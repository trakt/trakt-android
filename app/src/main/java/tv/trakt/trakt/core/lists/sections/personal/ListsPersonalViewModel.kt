package tv.trakt.trakt.core.lists.sections.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase

@OptIn(FlowPreview::class)
internal class ListsPersonalViewModel(
    private val listId: TraktId,
    private val getListUseCase: GetPersonalListsUseCase,
    private val getListItemsUseCase: GetPersonalListItemsUseCase,
    private val localListsSource: ListsPersonalLocalDataSource,
    private val localListsItemsSource: ListsPersonalItemsLocalDataSource,
) : ViewModel() {
    private val initialState = ListsPersonalState()

    private val userState = MutableStateFlow(initialState.user)
    private val listState = MutableStateFlow(initialState.list)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        observeLists()
    }

    private fun observeLists() {
        viewModelScope.launch {
            merge(
                localListsSource.observeUpdates(),
                localListsItemsSource.observeUpdates(),
            )
                .distinctUntilChanged()
                .debounce(250)
                .collect {
                    loadLocalData()
                }
        }
    }

    private fun loadLocalData() {
        viewModelScope.launch {
            try {
                listState.update {
                    getListUseCase.getLocalList(listId)
                }
                itemsState.update {
                    getListItemsUseCase.getLocalItems(listId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            }
        }
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            try {
                listState.update {
                    getListUseCase.getLocalList(listId)
                }

                val localItems = getListItemsUseCase.getLocalItems(listId)

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getListItemsUseCase.getItems(
                        listId = listId,
                        limit = LISTS_SECTION_LIMIT,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    val state: StateFlow<ListsPersonalState> = combine(
        listState,
        userState,
        itemsState,
        loadingState,
        errorState,
    ) { s1, s2, s3, s4, s5 ->
        ListsPersonalState(
            list = s1,
            user = s2,
            items = s3,
            loading = s4,
            error = s5,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
