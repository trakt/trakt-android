package tv.trakt.trakt.core.users.sections.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
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
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.users.UserProfileConfig.HORIZONTAL_SECTION_LIMIT
import tv.trakt.trakt.core.users.sections.history.usecases.GetUserProfileHistoryUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class UserProfileHistoryViewModel(
    private val userId: TraktId,
    private val getHistoryUseCase: GetUserProfileHistoryUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = UserProfileHistoryState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.USER_PROFILE_HISTORY))

    private var collapseJob: Job? = null

    init {
        loadData()
    }

    fun loadData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            try {
                if (itemsState.value == initialState.items) {
                    loadingState.update { Loading }
                }

                itemsState.update {
                    getHistoryUseCase.getUserHistory(
                        userId = userId,
                        pagination = Pagination(
                            page = 1,
                            limit = HORIZONTAL_SECTION_LIMIT,
                        ),
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }
        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.USER_PROFILE_HISTORY)
                else -> collapsingManager.expand(CollapsingKey.USER_PROFILE_HISTORY)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        itemsState,
        loadingState,
        errorState,
        collapseState,
    ) { state ->
        UserProfileHistoryState(
            items = state[0] as ImmutableList<HomeActivityItem>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
            collapsed = state[3] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
