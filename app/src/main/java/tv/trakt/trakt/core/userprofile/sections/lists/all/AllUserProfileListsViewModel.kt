package tv.trakt.trakt.core.userprofile.sections.lists.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Collaborations
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Liked
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Personal
import tv.trakt.trakt.core.userprofile.UserProfileConfig.ALL_PAGE_LIMIT
import tv.trakt.trakt.core.userprofile.sections.lists.all.AllUserProfileListsState.User
import tv.trakt.trakt.core.userprofile.sections.lists.usecases.GetUserProfileListsUseCase

internal class AllUserProfileListsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getListsUseCase: GetUserProfileListsUseCase,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<AllUserProfileListsDestination>()

    private val userId = destination.userId.toTraktId()
    private val userName = destination.userName

    private val initialState = AllUserProfileListsState(
        user = User(userId, userName),
        filter = destination.initialFilter,
    )

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var page = 1
    private var hasMorePages = false

    private var dataJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                page = 1
                hasMorePages = false

                loadingState.update { Loading }
                loadingMoreState.update { Idle }

                itemsState.update {
                    val pagination = Pagination(page, ALL_PAGE_LIMIT)
                    when (filterState.value) {
                        Personal -> getListsUseCase.getPersonalLists(
                            userId = userId,
                            pagination = pagination,
                        )
                        Collaborations -> getListsUseCase.getCollaborationLists(
                            userId = userId,
                            pagination = pagination,
                        )
                        Liked -> EmptyImmutableList
                    }
                }

                hasMorePages = filterState.value == Personal &&
                    (itemsState.value?.size ?: 0) >= ALL_PAGE_LIMIT
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
        if (!hasMorePages || loadingMoreState.value.isLoading || loadingState.value.isLoading) return
        if (filterState.value != Personal) return

        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                page += 1
                loadingMoreState.update { Loading }
                val pagination = Pagination(page, ALL_PAGE_LIMIT)
                val newItems = getListsUseCase.getPersonalLists(userId, pagination)
                if (newItems.isNotEmpty()) {
                    itemsState.update { current ->
                        current?.plus(newItems)
                            ?.sortedByDescending { it.updatedAt }
                            ?.toImmutableList()
                    }
                }
                hasMorePages = newItems.size >= ALL_PAGE_LIMIT
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

    fun setFilter(filter: PersonalListType) {
        if (filterState.value == filter) return

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
        userState,
        itemsState,
        filterState,
        loadingState,
        loadingMoreState,
        errorState,
    ) { state ->
        AllUserProfileListsState(
            user = state[0] as User,
            items = state[1] as? ImmutableList<CustomList>,
            filter = state[2] as PersonalListType,
            loading = state[3] as LoadingState,
            loadingMore = state[4] as LoadingState,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
