package tv.trakt.trakt.app.core.shows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.shows.model.AnticipatedShow
import tv.trakt.trakt.app.core.shows.model.TrendingShow
import tv.trakt.trakt.app.core.shows.usecase.GetAnticipatedShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetPopularShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetTrendingShowsUseCase
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Show

internal class ShowsViewModel(
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getPopularShowsUseCase: GetPopularShowsUseCase,
    private val getAnticipatedShowsUseCase: GetAnticipatedShowsUseCase,
    private val getRecommendedShowsUseCase: GetRecommendedShowsUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = ShowsState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val trendingShowsState = MutableStateFlow(initialState.trendingShows)
    private val popularShowsState = MutableStateFlow(initialState.popularShows)
    private val anticipatedShowsState = MutableStateFlow(initialState.anticipatedShows)
    private val recommendedShowsState = MutableStateFlow(initialState.recommendedShows)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }
                coroutineScope {
                    val trendingShowsAsync = async { getTrendingShowsUseCase.getTrendingShows() }
                    val popularShowsAsync = async { getPopularShowsUseCase.getPopularShows() }
                    val anticipatedShowsAsync = async { getAnticipatedShowsUseCase.getAnticipatedShows() }

                    val recommendedShowsAsync = async {
                        if (sessionManager.isAuthenticated()) {
                            getRecommendedShowsUseCase.getRecommendedShows()
                        } else {
                            null
                        }
                    }

                    val trendingShows = trendingShowsAsync.await()
                    val popularShows = popularShowsAsync.await()
                    val anticipatedShows = anticipatedShowsAsync.await()
                    val recommendedShows = recommendedShowsAsync.await()

                    trendingShowsState.value = trendingShows
                    popularShowsState.value = popularShows
                    anticipatedShowsState.value = anticipatedShows
                    recommendedShowsState.value = recommendedShows
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error, error.toString())
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowsState> = combine(
        loadingState,
        trendingShowsState,
        popularShowsState,
        anticipatedShowsState,
        recommendedShowsState,
        errorState,
    ) { s ->
        ShowsState(
            isLoading = s[0] as Boolean,
            trendingShows = s[1] as ImmutableList<TrendingShow>?,
            popularShows = s[2] as ImmutableList<Show>?,
            anticipatedShows = s[3] as ImmutableList<AnticipatedShow>?,
            recommendedShows = s[4] as ImmutableList<Show>?,
            error = s[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
