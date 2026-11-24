package tv.trakt.trakt.core.lists.features.convert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListDraft
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ITEMS_ALL_LIMIT
import tv.trakt.trakt.core.lists.features.convert.navigation.SmartListConvertDestination
import tv.trakt.trakt.core.lists.features.convert.usecases.ConvertToSmartListUseCase
import tv.trakt.trakt.core.lists.features.convert.usecases.GetPersonalListUseCase
import tv.trakt.trakt.core.lists.features.convert.usecases.GetSmartListDraftItemsUseCase
import tv.trakt.trakt.core.lists.features.convert.usecases.GetSmartListDraftUseCase
import tv.trakt.trakt.core.lists.features.details.usecases.GetListItemsUseCase
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.model.SmartListItem

private const val SCREEN_NAME = "smart_list_convert"

@Suppress("UNCHECKED_CAST")
internal class SmartListConvertViewModel(
    savedStateHandle: SavedStateHandle,
    private val getPersonalListUseCase: GetPersonalListUseCase,
    private val getSmartListDraftUseCase: GetSmartListDraftUseCase,
    private val getListItemsUseCase: GetListItemsUseCase,
    private val getSmartListDraftItemsUseCase: GetSmartListDraftItemsUseCase,
    private val convertToSmartListUseCase: ConvertToSmartListUseCase,
    private val dataChanges: DataChanges,
    private val analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<SmartListConvertDestination>()
    private val listId = destination.listId.toTraktId()

    private val initialState = SmartListConvertState()

    private val listState = MutableStateFlow(initialState.list)
    private val draftState = MutableStateFlow(initialState.draft)
    private val currentItemsState = MutableStateFlow(initialState.currentItems)
    private val proposedItemsState = MutableStateFlow(initialState.proposedItems)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val convertingState = MutableStateFlow(initialState.converting)
    private val missingState = MutableStateFlow(initialState.missing)
    private val convertedListState = MutableStateFlow(initialState.convertedList)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        analytics.logScreenView(
            screenName = SCREEN_NAME,
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                coroutineScope {
                    val list = async { getPersonalListUseCase.getList(listId) }
                    val draft = async { getSmartListDraftUseCase.getDraft(listId) }
                    val currentItems = async { loadCurrentItems() }
                    val proposedItems = async {
                        getSmartListDraftItemsUseCase.getItems(
                            listId = listId,
                            pagination = Pagination(1, LISTS_ITEMS_ALL_LIMIT),
                        )
                    }

                    val draftResult = draft.await()
                    if (draftResult == null) {
                        missingState.update { true }
                        return@coroutineScope
                    }

                    listState.update { list.await() }
                    draftState.update { draftResult }
                    currentItemsState.update { currentItems.await() }
                    proposedItemsState.update { proposedItems.await() }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                    missingState.update { true }
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private suspend fun loadCurrentItems(): ImmutableList<SmartListItem> {
        return getListItemsUseCase.getItems(
            listId = listId,
            type = listOf(MediaType.Show, MediaType.Movie),
            sorting = Sorting.Default,
            pagination = Pagination(1, LISTS_ITEMS_ALL_LIMIT),
            filters = null,
        ).mapNotNull {
            when (it) {
                is CustomListItem.MovieItem -> SmartListItem.MovieItem(it.movie)
                is CustomListItem.ShowItem -> SmartListItem.ShowItem(it.show)
                is CustomListItem.SeasonItem, is CustomListItem.EpisodeItem -> null
            }
        }.distinctBy { it.key }
            .toImmutableList()
    }

    fun convert() {
        val list = listState.value ?: return
        if (convertingState.value.isLoading) return

        viewModelScope.launch {
            try {
                convertingState.update { Loading }
                analytics.lists.logSmartListCreate()

                val smartList = convertToSmartListUseCase.convert(list)

                dataChanges.notify(Kind.Lists, targetId = list.ids.trakt.value)
                convertedListState.update { smartList }
                convertingState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                    errorState.update { error }
                    convertingState.update { Idle }
                }
            }
        }
    }

    fun clearError() {
        errorState.update { null }
    }

    val state = combine(
        listState,
        draftState,
        currentItemsState,
        proposedItemsState,
        loadingState,
        convertingState,
        missingState,
        convertedListState,
        errorState,
    ) { state ->
        SmartListConvertState(
            list = state[0] as CustomList?,
            draft = state[1] as SmartListDraft?,
            currentItems = state[2] as ImmutableList<SmartListItem>?,
            proposedItems = state[3] as ImmutableList<SmartListItem>?,
            loading = state[4] as LoadingState,
            converting = state[5] as LoadingState,
            missing = state[6] as Boolean,
            convertedList = state[7] as SmartList?,
            error = state[8] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
