package tv.trakt.trakt.core.lists.features.addfrom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.lists.usecases.AddPersonalListItemUseCase
import tv.trakt.trakt.common.core.lists.usecases.ListItemLimitException
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsItem
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsPick
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsSource
import tv.trakt.trakt.core.lists.features.addfrom.model.toggleAllPicks
import tv.trakt.trakt.core.lists.features.addfrom.model.togglePick
import tv.trakt.trakt.core.lists.features.addfrom.usecases.GetAddFromListsUseCase

private const val PAGE_LIMIT = 50

@Suppress("UNCHECKED_CAST")
internal class AddFromListsViewModel(
    private val list: CustomList,
    private val sessionManager: SessionManager,
    private val getAddFromListsUseCase: GetAddFromListsUseCase,
    private val addListItemUseCase: AddPersonalListItemUseCase,
    private val dataChanges: DataChanges,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = AddFromListsState()

    private val sourcesState = MutableStateFlow(initialState.sources)
    private val activeSourceState = MutableStateFlow(initialState.activeSource)
    private val itemsState = MutableStateFlow(initialState.items)
    private val hasMoreItemsState = MutableStateFlow(initialState.hasMoreItems)
    private val listedKeysState = MutableStateFlow(initialState.listedKeys)
    private val picksState = MutableStateFlow(initialState.picks)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val addingState = MutableStateFlow(initialState.adding)
    private val errorState = MutableStateFlow(initialState.error)

    private val resultFlow = MutableSharedFlow<AddFromListsResult>(replay = 0)
    val results = resultFlow.asSharedFlow()

    private var itemsJob: Job? = null
    private var page = 1

    init {
        loadSources()
        loadListedKeys()
    }

    private fun loadSources() {
        viewModelScope.launch {
            try {
                loadingState.update { LoadingState.Loading }
                val user = sessionManager.getProfile()
                if (user == null) {
                    loadingState.update { LoadingState.Done }
                    return@launch
                }

                val sources = getAddFromListsUseCase.getSources(
                    userId = user.ids.trakt,
                    targetListId = list.ids.trakt,
                )

                sourcesState.update { sources }
                selectSource(sources.first())
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    loadingState.update { LoadingState.Done }
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadListedKeys() {
        viewModelScope.launch {
            try {
                val keys = getAddFromListsUseCase.getListedKeys(list.ids.trakt)
                listedKeysState.update { keys }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    listedKeysState.update { persistentSetOf() }
                    Timber.recordError(error)
                }
            }
        }
    }

    fun selectSource(source: AddFromListsSource) {
        if (source == activeSourceState.value) {
            return
        }

        activeSourceState.update { source }
        loadItems(source)
    }

    private fun loadItems(source: AddFromListsSource) {
        itemsJob?.cancel()
        itemsJob = viewModelScope.launch {
            try {
                page = 1
                itemsState.update { null }
                errorState.update { null }
                hasMoreItemsState.update { false }
                loadingState.update { LoadingState.Loading }

                val items = getAddFromListsUseCase.getItems(
                    source = source,
                    pagination = Pagination(page, PAGE_LIMIT),
                )

                itemsState.update { items.distinctBy { it.key }.toImmutableList() }
                hasMoreItemsState.update { items.size >= PAGE_LIMIT }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { LoadingState.Done }
            }
        }
    }

    fun loadMoreItems() {
        val source = activeSourceState.value ?: return
        if (!hasMoreItemsState.value || loadingState.value.isLoading || loadingMoreState.value.isLoading) {
            return
        }

        itemsJob = viewModelScope.launch {
            try {
                loadingMoreState.update { LoadingState.Loading }

                val nextPage = page + 1
                val items = getAddFromListsUseCase.getItems(
                    source = source,
                    pagination = Pagination(nextPage, PAGE_LIMIT),
                )

                itemsState.update { current ->
                    current.orEmpty()
                        .plus(items)
                        .distinctBy { it.key }
                        .toImmutableList()
                }
                hasMoreItemsState.update { items.size >= PAGE_LIMIT }
                page = nextPage
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingMoreState.update { LoadingState.Done }
            }
        }
    }

    fun togglePick(item: AddFromListsItem) {
        val source = activeSourceState.value ?: return
        if (listedKeysState.value?.contains(item.key) == true) {
            return
        }

        picksState.update {
            it.togglePick(AddFromListsPick.from(item, source.key)).toImmutableList()
        }
    }

    fun toggleAllPicks(candidates: List<AddFromListsPick>) {
        picksState.update {
            it.toggleAllPicks(candidates).toImmutableList()
        }
    }

    fun addPicks() {
        val listed = listedKeysState.value ?: return
        val picks = picksState.value.filter { it.key !in listed }
        if (picks.isEmpty() || addingState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                addingState.update { LoadingState.Loading }

                addListItemUseCase.addItems(
                    listId = list.ids.trakt,
                    ownerId = list.user.ids.trakt,
                    movieIds = picks.filter { it.type == MediaType.Movie }.map { it.id },
                    showIds = picks.filter { it.type == MediaType.Show }.map { it.id },
                )

                dataChanges.notify(Kind.Lists, targetId = list.ids.trakt.value)
                analytics.lists.logListItemAdd()
                resultFlow.emit(AddFromListsResult.Added(picks.size))
            } catch (error: ListItemLimitException) {
                resultFlow.emit(AddFromListsResult.LimitReached(error))
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
                resultFlow.emit(AddFromListsResult.Failed)
            } finally {
                addingState.update { LoadingState.Done }
            }
        }
    }

    val state = combine(
        sourcesState,
        activeSourceState,
        itemsState,
        hasMoreItemsState,
        listedKeysState,
        picksState,
        loadingState,
        loadingMoreState,
        addingState,
        errorState,
    ) { state ->
        AddFromListsState(
            sources = state[0] as ImmutableList<AddFromListsSource>,
            activeSource = state[1] as AddFromListsSource?,
            items = state[2] as ImmutableList<AddFromListsItem>?,
            hasMoreItems = state[3] as Boolean,
            listedKeys = state[4] as ImmutableSet<String>?,
            picks = state[5] as ImmutableList<AddFromListsPick>,
            loading = state[6] as LoadingState,
            loadingMore = state[7] as LoadingState,
            adding = state[8] as LoadingState,
            error = state[9] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
