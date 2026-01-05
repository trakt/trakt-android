package tv.trakt.trakt.core.discover.sections.recommended

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.interleave
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class DiscoverRecommendedViewModel(
    private val modeManager: MediaModeManager,
    private val getRecommendedShowsUseCase: GetRecommendedShowsUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = DiscoverRecommendedState()

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
                    val showsAsync = async { getRecommendedShowsUseCase.getShows() }
                    val moviesAsync = async { getRecommendedMoviesUseCase.getMovies() }

                    val shows = if (modeState.value.isMediaOrShows) showsAsync.await() else emptyList()
                    val movies = if (modeState.value.isMediaOrMovies) moviesAsync.await() else emptyList()

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
                loadingState.update { DONE }
                dataJob = null
            }
        }
    }

    private suspend fun loadLocalData() {
        return coroutineScope {
            val localShowsAsync = async { getRecommendedShowsUseCase.getLocalShows() }
            val localMoviesAsync = async { getRecommendedMoviesUseCase.getLocalMovies() }

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

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            val key = when (modeState.value) {
                MediaMode.MEDIA -> CollapsingKey.DISCOVER_MEDIA_RECOMMENDED
                MediaMode.SHOWS -> CollapsingKey.DISCOVER_SHOWS_RECOMMENDED
                MediaMode.MOVIES -> CollapsingKey.DISCOVER_MOVIES_RECOMMENDED
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
                MediaMode.MEDIA -> CollapsingKey.DISCOVER_MEDIA_RECOMMENDED
                MediaMode.SHOWS -> CollapsingKey.DISCOVER_SHOWS_RECOMMENDED
                MediaMode.MOVIES -> CollapsingKey.DISCOVER_MOVIES_RECOMMENDED
            },
        )
    }

    val state = combine(
        itemsState,
        modeState,
        collapseState,
        loadingState,
        errorState,
    ) { s1, s2, s3, s4, s5 ->
        DiscoverRecommendedState(
            items = s1,
            mode = s2,
            collapsed = s3,
            loading = s4,
            error = s5,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
