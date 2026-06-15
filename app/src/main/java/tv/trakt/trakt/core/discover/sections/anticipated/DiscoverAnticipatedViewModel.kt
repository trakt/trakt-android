@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.discover.sections.anticipated

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
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.filters.data.GlobalFilterManager

internal class DiscoverAnticipatedViewModel(
    private val filterManager: GlobalFilterManager,
    private val getAnticipatedShowsUseCase: GetAnticipatedShowsUseCase,
    private val getAnticipatedMoviesUseCase: GetAnticipatedMoviesUseCase,
) : ViewModel() {
    private val initialState = DiscoverAnticipatedState()

    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null

    init {
        loadData()
        observeMode()
    }

    private fun observeMode() {
        filterManager.observeFilter()
            .onEach { value ->
                filterState.update { value }
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
                    val showsAsync = async { getAnticipatedShowsUseCase.getShows(filters = filterState.value) }
                    val moviesAsync = async { getAnticipatedMoviesUseCase.getMovies(filters = filterState.value) }

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

        Timber.d("Loading anticipated data for mode: ${filterState.value}")
    }

    private suspend fun loadLocalData() {
        return coroutineScope {
            val localShowsAsync = async { getAnticipatedShowsUseCase.getLocalShows() }
            val localMoviesAsync = async { getAnticipatedMoviesUseCase.getLocalMovies() }

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

    val state = combine(
        itemsState,
        filterState,
        loadingState,
        errorState,
    ) { state ->
        DiscoverAnticipatedState(
            items = state[0] as ImmutableList<DiscoverItem>?,
            filter = state[1] as GlobalFilter?,
            loading = state[2] as LoadingState,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
