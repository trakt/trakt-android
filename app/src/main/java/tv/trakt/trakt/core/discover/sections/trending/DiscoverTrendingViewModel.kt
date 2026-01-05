@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.discover.sections.trending

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
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.interleave
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class DiscoverTrendingViewModel(
    private val modeManager: MediaModeManager,
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = DiscoverTrendingState()

    private val modeState = MutableStateFlow(modeManager.getMode())
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
        modeManager.observeMode()
            .onEach { value ->
                modeState.update { value }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadLocalData()
                loadRemoteData()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
                dataJob = null
            }
        }

        Timber.d("Loading trending data for mode: ${modeState.value}")
    }

    private suspend fun loadLocalData() {
        return coroutineScope {
            val localShowsAsync = async { getTrendingShowsUseCase.getLocalShows() }
            val localMoviesAsync = async { getTrendingMoviesUseCase.getLocalMovies() }

            val localShows = if (modeState.value.isMediaOrShows) localShowsAsync.await() else emptyList()
            val localMovies = if (modeState.value.isMediaOrMovies) localMoviesAsync.await() else emptyList()

            if (localShows.isNotEmpty() || localMovies.isNotEmpty()) {
                itemsState.update {
                    listOf(localShows, localMovies)
                        .interleave()
                        .toImmutableList()
                }
                loadingState.update { DONE }
            } else {
                loadingState.update { LOADING }
            }
        }
    }

    private suspend fun loadRemoteData() {
        return coroutineScope {
            val showsAsync = async { getTrendingShowsUseCase.getShows() }
            val moviesAsync = async { getTrendingMoviesUseCase.getMovies() }

            val shows = if (modeState.value.isMediaOrShows) showsAsync.await() else emptyList()
            val movies = if (modeState.value.isMediaOrMovies) moviesAsync.await() else emptyList()

            itemsState.update {
                listOf(shows, movies)
                    .interleave()
                    .toImmutableList()
            }
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            val key = when (modeState.value) {
                MediaMode.MEDIA -> CollapsingKey.DISCOVER_MEDIA_TRENDING
                MediaMode.SHOWS -> CollapsingKey.DISCOVER_SHOWS_TRENDING
                MediaMode.MOVIES -> CollapsingKey.DISCOVER_MOVIES_TRENDING
            }
            when {
                collapsed -> collapsingManager.collapse(key)
                else -> collapsingManager.expand(key)
            }
        }
    }

    private fun isCollapsed(): Boolean {
        return collapsingManager.isCollapsed(
            key = when (modeState.value) {
                MediaMode.MEDIA -> CollapsingKey.DISCOVER_MEDIA_TRENDING
                MediaMode.SHOWS -> CollapsingKey.DISCOVER_SHOWS_TRENDING
                MediaMode.MOVIES -> CollapsingKey.DISCOVER_MOVIES_TRENDING
            },
        ).also {
            Timber.d("Trending isCollapsed for mode ${modeState.value}: $it")
        }
    }

    val state = combine(
        itemsState,
        modeState,
        collapseState,
        loadingState,
        errorState,
    ) { state ->
        DiscoverTrendingState(
            items = state[0] as ImmutableList<DiscoverItem>?,
            mode = state[1] as MediaMode?,
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
