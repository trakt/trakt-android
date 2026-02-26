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
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.ListsConfig
import tv.trakt.trakt.core.lists.features.all.navigation.AllListsDestination
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedLocalDataSource
import tv.trakt.trakt.core.lists.sections.liked.usecases.GetLikedListsUseCase
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Liked
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Personal
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase

internal class AllListsViewModel(
    savedStateHandle: SavedStateHandle,
    analytics: Analytics,
    private val sessionManager: SessionManager,
    private val getPersonalListsUseCase: GetPersonalListsUseCase,
    private val getLikedListsUseCase: GetLikedListsUseCase,
    private val localPersonalListsSource: ListsPersonalLocalDataSource,
    private val localLikedListsSource: ListsLikedLocalDataSource,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<AllListsDestination>()

    private val initialState = AllListsState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(destination.initialFilter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var page = 1
    private var hasMorePages = false

    private var dataJob: Job? = null

    init {
        observeLists()
        loadData()

        analytics.logScreenView(
            screenName = "all_lists",
        )
    }

    private fun observeLists() {
        merge(
            localPersonalListsSource.observeUpdates(),
            localLikedListsSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach { loadData() }
            .launchIn(viewModelScope)
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
                        Personal -> getPersonalListsUseCase.getLocalLists(pagination)
                        Liked -> getLikedListsUseCase.getLocalLists(pagination)
                        else -> EmptyImmutableList
                    }.also { items ->
                        itemsState.update { items }
                    }

                    if (localLists.isNotEmpty() && (localLists.size < ListsConfig.LISTS_PAGE_LIMIT)) {
                        loadingState.update { DONE }
                        hasMorePages = localLists.size >= ListsConfig.LISTS_ALL_PAGE_LIMIT
                        return@launch
                    } else {
                        if (localLists.isEmpty()) {
                            loadingState.update { LOADING }
                        } else {
                            loadingState.update { DONE }
                            loadingMoreState.update { LOADING }
                        }
                    }
                }

                if (reload) {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    val pagination = Pagination(page, ListsConfig.LISTS_ALL_PAGE_LIMIT)
                    when (filterState.value) {
                        Personal -> getPersonalListsUseCase.getLists(pagination, notify = reload)
                        Liked -> getLikedListsUseCase.getLists(pagination)
                        else -> EmptyImmutableList
                    }
                }

                hasMorePages = (itemsState.value?.size ?: 0) >= ListsConfig.LISTS_ALL_PAGE_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
                loadingMoreState.update { DONE }
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
                loadingMoreState.update { LOADING }

                val pagination = Pagination(page, ListsConfig.LISTS_ALL_PAGE_LIMIT)

                val newItems = when (filterState.value) {
                    Personal -> getPersonalListsUseCase.getLists(pagination, notify = false)
                    Liked -> getLikedListsUseCase.getLists(pagination)
                    else -> EmptyImmutableList
                }

                if (newItems.isNotEmpty()) {
                    itemsState.update {
                        it
                            ?.plus(newItems)
                            ?.toImmutableList()
                    }
                }

                hasMorePages = newItems.size >= ListsConfig.LISTS_ALL_PAGE_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingMoreState.update { DONE }
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { EmptyImmutableList }
            loadingState.update { DONE }
            loadingMoreState.update { DONE }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { IDLE }
            loadingMoreState.update { IDLE }
        }

        return false
    }

    fun setFilter(filter: PersonalListType) {
        if (filterState.value == filter) {
            return
        }

        filterState.update { filter }
        itemsState.update { null }

        loadingState.update { IDLE }
        loadingMoreState.update { IDLE }

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
        errorState,
    ) { state ->
        AllListsState(
            items = state[0] as ImmutableList<CustomList>?,
            filter = state[1] as PersonalListType?,
            loading = state[2] as LoadingState,
            loadingMore = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
