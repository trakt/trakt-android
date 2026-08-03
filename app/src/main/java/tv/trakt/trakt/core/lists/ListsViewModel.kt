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
import tv.trakt.trakt.common.model.lists.ListsItem
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.ListsState.UserState
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
import tv.trakt.trakt.core.lists.usecases.GetListsFilterUseCase
import tv.trakt.trakt.resources.R
import kotlin.time.Duration.Companion.milliseconds

@Suppress("UNCHECKED_CAST")
@OptIn(FlowPreview::class)
internal class ListsViewModel(
    private val sessionManager: SessionManager,
    private val getPersonalListsUseCase: GetPersonalListsUseCase,
    private val getLikedListsUseCase: GetLikedListsUseCase,
    private val getCollaborationsListsUseCase: GetCollaborationsListsUseCase,
    private val getSmartListsUseCase: GetSmartListsUseCase,
    private val deleteSmartListUseCase: DeleteSmartListUseCase,
    private val localListsSource: ListsPersonalLocalDataSource,
    private val localLikedListsSource: ListsLikedLocalDataSource,
    private val localCollaborationsListsSource: ListsCollaborationsLocalDataSource,
    private val localSmartListsSource: ListsSmartLocalDataSource,
    private val getFilterUseCase: GetListsFilterUseCase,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = ListsState()

    private val userState = MutableStateFlow(initialState.user)
    private val filterState = MutableStateFlow(initialState.filter)
    private val listsState = MutableStateFlow(initialState.lists)
    private val listsLoadingState = MutableStateFlow(initialState.listsLoading)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null

    init {
        observeUser()
        observeLists()

        analytics.logScreenView(
            screenName = "lists",
        )
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .onEach { user ->
                userState.update {
                    UserState(
                        user = user,
                        loading = Done,
                    )
                }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeLists() {
        merge(
            localListsSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                loadLocalData()
            }
            .launchIn(viewModelScope)

        merge(
            localLikedListsSource.observeUpdates(),
            localCollaborationsListsSource.observeUpdates(),
            localSmartListsSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
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
                    Smart -> getSmartListsUseCase.getLocalSmartLists(pagination)
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
                }
                listsState.update {
                    localLists.toImmutableList()
                }
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
            loadFilter()

            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val pagination = Pagination(1, ListsConfig.LISTS_PAGE_LIMIT)

                val localLists = when (filterState.value) {
                    Smart -> getSmartListsUseCase.getLocalSmartLists(pagination)
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
                }

                if (localLists.isNotEmpty()) {
                    listsState.update {
                        localLists.toImmutableList()
                    }
                    listsLoadingState.update { Done }
                } else {
                    listsLoadingState.update { Loading }
                }

                val lists = when (filterState.value) {
                    Smart -> getSmartListsUseCase.getSmartLists(pagination)
                        .sortedByDescending { it.updatedAt }
                        .map(ListsItem::Smart)
                    Personal -> getPersonalListsUseCase.getLists(pagination)
                        .sortedByDescending { it.updatedAt }
                        .map(ListsItem::Custom)
                    Collaborations -> getCollaborationsListsUseCase.getLists()
                        .sortedByDescending { it.updatedAt }
                        .map(ListsItem::Custom)
                    Liked -> getLikedListsUseCase.getLists(pagination)
                        .map(ListsItem::Custom)
                }
                listsState.update {
                    lists.toImmutableList()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                listsLoadingState.update { Done }
                dataJob = null
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

    private suspend fun loadFilter(): PersonalListType {
        val filter = getFilterUseCase.getFilter()
        filterState.update { filter }
        return filter
    }

    fun setFilter(filter: PersonalListType) {
        if (filterState.value == filter) {
            return
        }

        filterState.update { filter }
        listsState.update { null }
        listsLoadingState.update { Idle }

        viewModelScope.launch {
            getFilterUseCase.setFilter(filter)
            loadData()
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            listsState.update { EmptyImmutableList }
            listsLoadingState.update { Done }
            return true
        } else {
            listsState.update { null }
            listsLoadingState.update { Idle }
        }

        return false
    }

    fun clearInfo() {
        infoState.update { null }
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
        infoState,
        errorState,
    ) { state ->
        ListsState(
            user = state[0] as UserState,
            filter = state[1] as PersonalListType,
            lists = state[2] as ImmutableList<ListsItem>?,
            listsLoading = state[3] as LoadingState,
            info = state[4] as DynamicStringResource?,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
