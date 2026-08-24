package tv.trakt.trakt.ui.components.dateselection.otherdatepicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.userprofile.sections.history.usecases.GetUserProfileHistoryUseCase

private const val PAGE_LIMIT = 50

@Suppress("UNCHECKED_CAST")
internal class OtherDatePickerViewModel(
    private val sessionManager: SessionManager,
    private val getHistoryUseCase: GetUserProfileHistoryUseCase,
) : ViewModel() {
    private val initialState = OtherDatePickerState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages = 1
    private var hasMoreData = false

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val remoteItems = getHistoryUseCase.getUserHistory(
                    userId = requireUserId(),
                    pagination = Pagination(page = 1, limit = PAGE_LIMIT),
                )

                itemsState.update { remoteItems }
                pages = 1
                hasMoreData = remoteItems.size >= PAGE_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun loadMoreData() {
        if (itemsState.value.isNullOrEmpty() || !hasMoreData) {
            return
        }
        if (loadingState.value.isLoading || loadingMoreState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingMoreState.update { Loading }

                val nextItems = getHistoryUseCase.getUserHistory(
                    userId = requireUserId(),
                    pagination = Pagination(page = pages + 1, limit = PAGE_LIMIT),
                )

                itemsState.update { items ->
                    (items.orEmpty() + nextItems).toImmutableList()
                }

                pages += 1
                hasMoreData = nextItems.size >= PAGE_LIMIT
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

    private suspend fun requireUserId(): TraktId {
        return checkNotNull(sessionManager.getProfile()) {
            "User profile is required to load watch history"
        }.ids.trakt
    }

    val state = combine(
        itemsState,
        loadingState,
        loadingMoreState,
        errorState,
    ) { state ->
        OtherDatePickerState(
            items = state[0] as ImmutableList<HomeActivityItem>?,
            loading = state[1] as LoadingState,
            loadingMore = state[2] as LoadingState,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
