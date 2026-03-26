package tv.trakt.trakt.core.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.ListsState.UserState
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.lists.ListsCollaborationsLocalDataSource
import tv.trakt.trakt.core.lists.sections.collaborations.usecases.GetCollaborationsListsUseCase
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedLocalDataSource
import tv.trakt.trakt.core.lists.sections.liked.usecases.GetLikedListsUseCase
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Collaborations
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Liked
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Personal
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase

@OptIn(FlowPreview::class)
internal class ListsViewModel(
    private val sessionManager: SessionManager,
    private val getPersonalListsUseCase: GetPersonalListsUseCase,
    private val getLikedListsUseCase: GetLikedListsUseCase,
    private val getCollaborationsListsUseCase: GetCollaborationsListsUseCase,
    private val localListsSource: ListsPersonalLocalDataSource,
    private val localListsItemsSource: ListsPersonalItemsLocalDataSource,
    private val localLikedListsSource: ListsLikedLocalDataSource,
    private val localCollaborationsListsSource: ListsCollaborationsLocalDataSource,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = ListsState()

    private val userState = MutableStateFlow(initialState.user)
    private val filterState = MutableStateFlow(initialState.filter)
    private val listsState = MutableStateFlow(initialState.lists)
    private val listsLoadingState = MutableStateFlow(initialState.listsLoading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var listsOrder: List<Int>? = null

    init {
        observeUser()
        observeLists()

        analytics.logScreenView(
            screenName = "lists",
        )
    }

    private fun observeUser() {
        viewModelScope.launch {
            sessionManager.observeProfile()
                .distinctUntilChanged()
                .collect { user ->
                    userState.update {
                        UserState(
                            user = user,
                            loading = DONE,
                        )
                    }
                    loadData()
                }
        }
    }

    private fun observeLists() {
        merge(
            localListsSource.observeUpdates(),
            localListsItemsSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadLocalData()
            }
            .launchIn(viewModelScope)

        merge(
            localLikedListsSource.observeUpdates(),
            localCollaborationsListsSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadLocalData() {
        viewModelScope.launch {
            try {
                val pagination = Pagination(1, ListsConfig.LISTS_PAGE_LIMIT)

                val localLists = when (filterState.value) {
                    Personal -> getPersonalListsUseCase.getLocalLists(pagination)
                    Liked -> getLikedListsUseCase.getLocalLists(pagination)
                    Collaborations -> getCollaborationsListsUseCase.getLocalLists(pagination)
                }
                listsState.update { sortLists(localLists) }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.value = error
                }
            }
        }
    }

    fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val pagination = Pagination(1, ListsConfig.LISTS_PAGE_LIMIT)

                val localLists = when (filterState.value) {
                    Personal -> getPersonalListsUseCase.getLocalLists(pagination)
                    Liked -> getLikedListsUseCase.getLocalLists(pagination)
                    Collaborations -> getCollaborationsListsUseCase.getLocalLists(pagination)
                }

                if (localLists.isNotEmpty()) {
                    listsState.update { sortLists(localLists) }
                    listsLoadingState.update { DONE }
                } else {
                    listsLoadingState.update { LOADING }
                }

                val lists = when (filterState.value) {
                    Personal -> getPersonalListsUseCase.getLists(pagination, notify = false)
                    Liked -> getLikedListsUseCase.getLists(pagination)
                    Collaborations -> getCollaborationsListsUseCase.getLists()
                }
                listsState.update { sortLists(lists) }

                listsOrder = listsState.value?.map { it.ids.trakt.value }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                listsLoadingState.update { DONE }
                dataJob = null
            }
        }
    }

    fun setFilter(filter: PersonalListType) {
        if (filterState.value == filter) {
            return
        }

        filterState.update { filter }
        listsState.update { null }
        listsLoadingState.update { IDLE }

        loadData()
    }

    private fun sortLists(lists: ImmutableList<CustomList>): ImmutableList<CustomList> {
        val order = listsOrder ?: return lists

        val orderMap = order
            .withIndex()
            .reversed()
            .associate {
                it.value to it.index
            }

        return lists
            .distinctBy { it.ids.trakt.value }
            .sortedBy {
                orderMap[it.ids.trakt.value] ?: Int.MIN_VALUE
            }
            .toImmutableList()
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            listsState.update { EmptyImmutableList }
            listsLoadingState.update { DONE }
            return true
        } else {
            listsState.update { null }
            listsLoadingState.update { IDLE }
        }

        return false
    }

    override fun onCleared() {
        dataJob?.cancel()
        super.onCleared()
    }

    val state = combine(
        userState,
        filterState,
        listsState,
        listsLoadingState,
        errorState,
    ) { s1, s2, s3, s4, s5 ->
        ListsState(
            user = s1,
            filter = s2,
            lists = s3,
            listsLoading = s4,
            error = s5,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
