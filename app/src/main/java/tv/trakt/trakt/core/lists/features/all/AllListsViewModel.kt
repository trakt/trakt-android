@file:OptIn(FlowPreview::class)

package tv.trakt.trakt.core.lists.features.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.ListsItem
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.ListsConfig
import tv.trakt.trakt.core.lists.features.all.navigation.AllListsDestination
import tv.trakt.trakt.core.lists.features.reorder.data.ReorderUpdates
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.lists.ListsCollaborationsLocalDataSource
import tv.trakt.trakt.core.lists.sections.collaborations.usecases.GetCollaborationsListsUseCase
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedLocalDataSource
import tv.trakt.trakt.core.lists.sections.liked.usecases.GetLikedListsUseCase
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Collaborations
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Liked
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Personal
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Smart
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase
import tv.trakt.trakt.core.lists.sections.smart.data.local.ListsSmartLocalDataSource
import tv.trakt.trakt.core.lists.sections.smart.usecases.DeleteSmartListUseCase
import tv.trakt.trakt.core.lists.sections.smart.usecases.GetSmartListsUseCase
import tv.trakt.trakt.resources.R
import kotlin.time.Duration.Companion.milliseconds

internal class AllListsViewModel(
    savedStateHandle: SavedStateHandle,
    analytics: Analytics,
    private val getPersonalListsUseCase: GetPersonalListsUseCase,
    private val getLikedListsUseCase: GetLikedListsUseCase,
    private val getCollaborationsListsUseCase: GetCollaborationsListsUseCase,
    private val getSmartListsUseCase: GetSmartListsUseCase,
    private val deleteSmartListUseCase: DeleteSmartListUseCase,
    private val localPersonalListsSource: ListsPersonalLocalDataSource,
    private val localLikedListsSource: ListsLikedLocalDataSource,
    private val localCollaborationsListsSource: ListsCollaborationsLocalDataSource,
    private val localSmartListsSource: ListsSmartLocalDataSource,
    private val reorderUpdates: ReorderUpdates,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<AllListsDestination>()

    private val initialState = AllListsState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(destination.initialFilter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var page = 1
    private var hasMorePages = false

    private var dataJob: Job? = null
    private var reorderJob: Job? = null

    init {
        loadUser()
        loadData()

        observeLists()

        analytics.logScreenView(
            screenName = "all_lists",
        )
    }

    private fun observeLists() {
        merge(
            localPersonalListsSource.observeUpdates(),
            localLikedListsSource.observeUpdates(),
            localCollaborationsListsSource.observeUpdates(),
            localSmartListsSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach { loadData() }
            .launchIn(viewModelScope)
    }

    private fun observeReorder(listIds: List<TraktId>) {
        reorderJob?.cancel()
        reorderJob = viewModelScope.launch {
            reorderUpdates.clear()
            listIds.forEach { listId ->
                launch {
                    reorderUpdates.observeUpdates(listId)
                        .distinctUntilChanged()
                        .debounce(200.milliseconds)
                        .collect { loadData(reload = true) }
                }
            }
        }
    }

    fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun loadData(reload: Boolean = false) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                page = 1
                hasMorePages = false

                if (!reload) {
                    val pagination = Pagination(page, ListsConfig.LISTS_ALL_PAGE_LIMIT)
                    val localLists = when (filterState.value) {
                        Smart -> getSmartListsUseCase.getLocalSmartLists()
                            .sortedByDescending { it.updatedAt }
                            .map(ListsItem::Smart)
                        Personal -> getPersonalListsUseCase.getLocalLists(pagination)
                            .sortedByDescending { it.updatedAt }
                            .map(ListsItem::Custom)
                        Collaborations -> getCollaborationsListsUseCase.getLocalLists()
                            .sortedByDescending { it.updatedAt }
                            .map(ListsItem::Custom)
                        Liked -> getLikedListsUseCase.getLocalLists(pagination)
                            .map(ListsItem::Custom)
                        else -> EmptyImmutableList
                    }.also { items ->
                        itemsState.update {
                            items.toImmutableList()
                        }
                    }

                    if (localLists.isNotEmpty() && (localLists.size < ListsConfig.LISTS_PAGE_LIMIT)) {
                        loadingState.update { Done }
                        hasMorePages = filterState.value != Collaborations &&
                            filterState.value != Smart &&
                            localLists.size >= ListsConfig.LISTS_ALL_PAGE_LIMIT
                        return@launch
                    } else {
                        if (localLists.isEmpty()) {
                            loadingState.update { Loading }
                        } else {
                            loadingState.update { Done }
                            loadingMoreState.update { Loading }
                        }
                    }
                }

                if (reload) {
                    loadingState.update { Loading }
                }

                itemsState.update {
                    val pagination = Pagination(page, ListsConfig.LISTS_ALL_PAGE_LIMIT)
                    when (filterState.value) {
                        Smart -> getSmartListsUseCase.getSmartLists()
                            .sortedByDescending { it.updatedAt }
                            .map(ListsItem::Smart)
                        Personal -> getPersonalListsUseCase.getLists(pagination, notify = reload)
                            .sortedByDescending { it.updatedAt }
                            .map(ListsItem::Custom)
                        Collaborations -> getCollaborationsListsUseCase.getLists()
                            .sortedByDescending { it.updatedAt }
                            .map(ListsItem::Custom)
                        Liked -> getLikedListsUseCase.getLists(pagination)
                            .map(ListsItem::Custom)
                        else -> EmptyImmutableList
                    }.toImmutableList()
                }

                hasMorePages = filterState.value != Collaborations &&
                    filterState.value != Smart &&
                    (itemsState.value?.size ?: 0) >= ListsConfig.LISTS_ALL_PAGE_LIMIT

                itemsState.value?.map { it.id }?.let {
                    observeReorder(it)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                loadingMoreState.update { Done }
            }
        }
    }

    fun loadMoreData() {
        if (!hasMorePages || loadingMoreState.value.isLoading || loadingState.value.isLoading) {
            return
        }

        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                page += 1
                loadingMoreState.update { Loading }

                val pagination = Pagination(page, ListsConfig.LISTS_ALL_PAGE_LIMIT)

                val newItems = when (filterState.value) {
                    Personal -> getPersonalListsUseCase.getLists(pagination)
                        .map(ListsItem::Custom)
                    Liked -> getLikedListsUseCase.getLists(pagination)
                        .map(ListsItem::Custom)
                    Collaborations, Smart, null -> EmptyImmutableList
                }

                if (newItems.isNotEmpty()) {
                    itemsState.update { state ->
                        val lists = state?.plus(newItems)
                        when (filterState.value) {
                            Personal -> lists?.sortedByDescending { it.updatedAt }
                            Collaborations -> lists?.sortedByDescending { it.updatedAt }
                            Liked -> lists
                            else -> lists
                        }?.toImmutableList()
                    }
                }

                hasMorePages = newItems.size >= ListsConfig.LISTS_ALL_PAGE_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingMoreState.update { Done }
            }
        }
    }

    fun deleteSmartList(listId: TraktId) {
        viewModelScope.launch {
            try {
                deleteSmartListUseCase.deleteList(listId)
                infoState.update {
                    DynamicStringResource(R.string.text_info_list_deleted)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            }
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { EmptyImmutableList }
            loadingState.update { Done }
            loadingMoreState.update { Done }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { Idle }
            loadingMoreState.update { Idle }
        }

        return false
    }

    fun setFilter(filter: PersonalListType) {
        if (filterState.value == filter) {
            return
        }

        filterState.update { filter }
        itemsState.update { null }

        loadingState.update { Idle }
        loadingMoreState.update { Idle }

        loadData()
    }

    override fun onCleared() {
        dataJob?.cancel()
        super.onCleared()
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        itemsState,
        filterState,
        loadingState,
        loadingMoreState,
        userState,
        errorState,
        infoState,
    ) { state ->
        AllListsState(
            items = state[0] as ImmutableList<ListsItem>?,
            filter = state[1] as PersonalListType?,
            loading = state[2] as LoadingState,
            loadingMore = state[3] as LoadingState,
            user = state[4] as User?,
            error = state[5] as Exception?,
            info = state[6] as DynamicStringResource?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
