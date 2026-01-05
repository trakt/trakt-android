package tv.trakt.trakt.core.discover.sections.anticipated

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
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class DiscoverAnticipatedViewModel(
    private val modeManager: MediaModeManager,
    private val getAnticipatedShowsUseCase: GetAnticipatedShowsUseCase,
    private val getAnticipatedMoviesUseCase: GetAnticipatedMoviesUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = DiscoverAnticipatedState()

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
                    val showsAsync = async { getAnticipatedShowsUseCase.getShows() }
                    val moviesAsync = async { getAnticipatedMoviesUseCase.getMovies() }

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

        Timber.d("Loading anticipated data for mode: ${modeState.value}")
    }

    private suspend fun loadLocalData() {
        return coroutineScope {
            val localShowsAsync = async { getAnticipatedShowsUseCase.getLocalShows() }
            val localMoviesAsync = async { getAnticipatedMoviesUseCase.getLocalMovies() }

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
                MediaMode.MEDIA -> CollapsingKey.DISCOVER_MEDIA_ANTICIPATED
                MediaMode.SHOWS -> CollapsingKey.DISCOVER_SHOWS_ANTICIPATED
                MediaMode.MOVIES -> CollapsingKey.DISCOVER_MOVIES_ANTICIPATED
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
                MediaMode.MEDIA -> CollapsingKey.DISCOVER_MEDIA_ANTICIPATED
                MediaMode.SHOWS -> CollapsingKey.DISCOVER_SHOWS_ANTICIPATED
                MediaMode.MOVIES -> CollapsingKey.DISCOVER_MOVIES_ANTICIPATED
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
        DiscoverAnticipatedState(
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
