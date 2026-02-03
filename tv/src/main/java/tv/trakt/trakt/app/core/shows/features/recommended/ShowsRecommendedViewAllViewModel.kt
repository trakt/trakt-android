package tv.trakt.trakt.app.core.shows.features.recommended

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.shows.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation

private const val ALL_ITEMS_LIMIT = 100

internal class ShowsRecommendedViewAllViewModel(
    private val getItemsUseCase: GetRecommendedShowsUseCase,
) : ViewModel() {
    private val initialState = ShowsRecommendedViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val showsState = MutableStateFlow(initialState.shows)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }

                val shows = getItemsUseCase.getRecommendedShows(
                    limit = ALL_ITEMS_LIMIT,
                    page = 1,
                )
                showsState.update { shows }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        showsState,
        errorState,
    ) { s1, s2, s3 ->
        ShowsRecommendedViewAllState(
            isLoading = s1,
            shows = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
