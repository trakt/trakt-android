@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.discover.sections.recommended

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.interleave
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode.MEDIA
import tv.trakt.trakt.common.model.MediaMode.MOVIES
import tv.trakt.trakt.common.model.MediaMode.SHOWS
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class DiscoverRecommendedViewModel(
    private val filterManager: GlobalFilterManager,
    private val getRecommendedShowsUseCase: GetRecommendedShowsUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = DiscoverRecommendedState()

    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val collapseState = MutableStateFlow(isCollapsed())
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var collapseJob: Job? = null

    init {
        loadData()
        observeMode()
    }

    private fun observeMode() {
        filterManager.observeFilter()
            .onEach { value ->
                filterState.update { value }
                collapseState.update { isCollapsed() }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadLocalData()
                coroutineScope {
                    val showsAsync = async { getRecommendedShowsUseCase.getShows(filters = filterState.value) }
                    val moviesAsync = async { getRecommendedMoviesUseCase.getMovies(filters = filterState.value) }

                    val shows = if (filterState.value.mode.isMediaOrShows) showsAsync.await() else emptyList()
                    val movies = if (filterState.value.mode.isMediaOrMovies) moviesAsync.await() else emptyList()

                    itemsState.update {
                        listOf(shows, movies)
                            .interleave()
                            .toImmutableList()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
            }
        }
    }

    private suspend fun loadLocalData() {
        return coroutineScope {
            val localShowsAsync = async { getRecommendedShowsUseCase.getLocalShows() }
            val localMoviesAsync = async { getRecommendedMoviesUseCase.getLocalMovies() }

            val localShows = if (filterState.value.mode.isMediaOrShows) localShowsAsync.await() else emptyList()
            val localMovies = if (filterState.value.mode.isMediaOrMovies) localMoviesAsync.await() else emptyList()

            if (localShows.isNotEmpty() || localMovies.isNotEmpty()) {
                itemsState.update {
                    listOf(localShows, localMovies)
                        .interleave()
                        .toImmutableList()
                }
                loadingState.update { Done }
            } else {
                loadingState.update { Loading }
            }
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            val key = when (filterState.value.mode) {
                MEDIA -> CollapsingKey.DISCOVER_MEDIA_RECOMMENDED
                SHOWS -> CollapsingKey.DISCOVER_SHOWS_RECOMMENDED
                MOVIES -> CollapsingKey.DISCOVER_MOVIES_RECOMMENDED
            }
            when {
                collapsed -> collapsingManager.collapse(key)
                else -> collapsingManager.expand(key)
            }
        }
    }

    private fun isCollapsed(): Boolean {
        return collapsingManager.isCollapsed(
            key = when (filterState.value.mode) {
                MEDIA -> CollapsingKey.DISCOVER_MEDIA_RECOMMENDED
                SHOWS -> CollapsingKey.DISCOVER_SHOWS_RECOMMENDED
                MOVIES -> CollapsingKey.DISCOVER_MOVIES_RECOMMENDED
            },
        )
    }

    val state = combine(
        itemsState,
        filterState,
        collapseState,
        loadingState,
        errorState,
    ) { state ->
        DiscoverRecommendedState(
            items = state[0] as ImmutableList<DiscoverItem>?,
            filter = state[1] as GlobalFilter?,
            collapsed = state[2] as Boolean,
            loading = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
