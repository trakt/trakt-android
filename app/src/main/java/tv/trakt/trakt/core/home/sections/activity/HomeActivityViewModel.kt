package tv.trakt.trakt.core.home.sections.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.core.home.sections.activity.usecases.GetSocialActivityUseCase

internal class HomeActivityViewModel(
    private val getSocialActivityUseCase: GetSocialActivityUseCase,
) : ViewModel() {
    private val initialState = HomeActivityState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                itemsState.update {
                    getSocialActivityUseCase.getSocialActivity(HOME_SECTION_LIMIT)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error loading social activity")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    val state: StateFlow<HomeActivityState> = combine(
        loadingState,
        itemsState,
        filterState,
        errorState,
    ) { s1, s2, s3, s4 ->
        HomeActivityState(
            loading = s1,
            items = s2,
            filter = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
