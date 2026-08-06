package tv.trakt.trakt.core.ratings.allratings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.ratings.allratings.usecases.GetSeasonRatingsUseCase

internal class AllRatingsViewModel(
    private val showId: TraktId?,
    private val getSeasonRatingsUseCase: GetSeasonRatingsUseCase,
) : ViewModel() {
    val initialState = AllRatingsState()

    private val seasonsState = MutableStateFlow(initialState.seasons)
    private val loadingState = MutableStateFlow(initialState.loading)

    init {
        loadData()
    }

    private fun loadData() {
        if (showId == null) return
        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                seasonsState.update {
                    getSeasonRatingsUseCase.getSeasonRatings(showId)
                }
                loadingState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    seasonsState.update { null }
                    loadingState.update { Idle }
                }
            }
        }
    }

    val state = combine(
        seasonsState,
        loadingState,
    ) { seasons, loading ->
        AllRatingsState(
            seasons = seasons,
            loading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
