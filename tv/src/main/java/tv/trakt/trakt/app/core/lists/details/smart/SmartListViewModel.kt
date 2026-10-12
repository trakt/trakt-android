package tv.trakt.trakt.app.core.lists.details.smart

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
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListState
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem
import tv.trakt.trakt.app.core.lists.details.smart.navigation.SmartListDestination
import tv.trakt.trakt.app.core.lists.details.smart.usecases.GetSmartListItemsUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.lists.SmartList

@Suppress("UNCHECKED_CAST")
internal class SmartListViewModel(
    savedStateHandle: SavedStateHandle,
    private val getListItemsUseCase: GetSmartListItemsUseCase,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<SmartListDestination>()
    private val initialState = PersonalListState()

    val list = Json.decodeFromString<SmartList>(destination.listJson)

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

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
                val items = getListItemsUseCase.getListItems(
                    listId = list.ids.trakt,
                    page = nextDataPage,
                )
                itemsState.update { items }
                hasMoreData = items.size >= PERSONAL_LIST_PAGE_LIMIT
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error loading smart list %d", list.ids.trakt.value)
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

                val items = getListItemsUseCase.getListItems(
                    listId = list.ids.trakt,
                    page = nextDataPage,
                )

                itemsState.update { current ->
                    current
                        ?.toPersistentList()
                        ?.plus(items)
                        ?.distinctBy { it.id }
                        ?.toImmutableList()
                }

                hasMoreData = items.size >= PERSONAL_LIST_PAGE_LIMIT
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error loading smart list %d page", list.ids.trakt.value)
                }
            } finally {
                loadingPageState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        loadingPageState,
        itemsState,
        collectionStateProvider.stateFlow,
        errorState,
    ) { state ->
        PersonalListState(
            isLoading = state[0] as Boolean,
            isLoadingPage = state[1] as Boolean,
            items = state[2] as ImmutableList<PersonalListItem>?,
            collection = state[3] as UserCollectionState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
