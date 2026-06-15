package tv.trakt.trakt.core.userprofile.sections.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
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
import tv.trakt.trakt.core.userprofile.UserProfileConfig.HORIZONTAL_SECTION_LIMIT
import tv.trakt.trakt.core.userprofile.sections.history.usecases.GetUserProfileHistoryUseCase

internal class UserProfileHistoryViewModel(
    private val userId: TraktId,
    private val getHistoryUseCase: GetUserProfileHistoryUseCase,
) : ViewModel() {
    private val initialState = UserProfileHistoryState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

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

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        itemsState,
        loadingState,
        errorState,
    ) { state ->
        UserProfileHistoryState(
            items = state[0] as ImmutableList<HomeActivityItem>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
