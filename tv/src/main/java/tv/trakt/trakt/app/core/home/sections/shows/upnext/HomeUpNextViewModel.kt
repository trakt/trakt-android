package tv.trakt.trakt.app.core.home.sections.shows.upnext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.Config.REFRESH_DATA_THRESHOLD_MINUTES
import tv.trakt.trakt.app.core.home.sections.shows.upnext.usecases.GetMoviesUpNextUseCase
import tv.trakt.trakt.app.core.home.sections.shows.upnext.usecases.GetShowsUpNextUseCase
import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncLocalSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalSource
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.data.local.MediaModeLocalSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.ifMovies
import tv.trakt.trakt.common.model.ifShows
import java.time.ZonedDateTime

internal class HomeUpNextViewModel(
    private val getShowsUpNextUseCase: GetShowsUpNextUseCase,
    private val getMoviesUpNextUseCase: GetMoviesUpNextUseCase,
    private val localShowsSyncSource: ShowsSyncLocalSource,
    private val localEpisodesSyncSource: EpisodesSyncLocalSource,
    private val dataChanges: DataChanges,
    private val appLifecycleProvider: AppLifecycleProvider,
    private val mediaModeLocalSource: MediaModeLocalSource,
) : ViewModel() {
    private val initialState = HomeUpNextState()
    private var mode = MediaMode.Media

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadedAt: ZonedDateTime? = null
    private var dataJob: Job? = null

    init {
        observeMode()
        observeApp()
        observeData()
    }

    private fun observeApp() {
        appLifecycleProvider.observeState(FOREGROUND)
            .filter {
                loadedAt != null &&
                    nowUtc().minusMinutes(REFRESH_DATA_THRESHOLD_MINUTES).isAfter(loadedAt)
            }
            .onEach {
                loadData(showLoading = false)
            }
            .launchIn(viewModelScope)
    }

    @OptIn(FlowPreview::class)
    private fun observeData() {
        dataChanges.observe(Kind.History)
            .debounce(250)
            .onEach {
                loadData(showLoading = false)
            }
            .launchIn(viewModelScope)
    }

    private fun observeMode() {
        mediaModeLocalSource.observeMode()
            .onEach {
                mode = it
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadData(showLoading: Boolean = true) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update { true }
                }

                coroutineScope {
                    val showsAsync = async {
                        mode.ifShows {
                            getShowsUpNextUseCase.getShowsUpNext()
                        }
                    }
                    val moviesAsync = async {
                        mode.ifMovies {
                            getMoviesUpNextUseCase.getMoviesUpNext()
                        }
                    }

                    val items = awaitAll(showsAsync, moviesAsync)
                        .flatten()
                        .distinctBy { it.key }
                        .sortedByDescending { it.sortKey }

                    itemsState.update {
                        items.toImmutableList()
                    }
                }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Failed to load data")
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun updateData() {
        Timber.d("updateData called")
        viewModelScope.launch {
            try {
                if (loadedAt == null) {
                    Timber.d("Data not loaded yet, skipping update")
                    return@launch
                }

                val localWatchlistUpdatedAt = localShowsSyncSource.getWatchlistUpdatedAt()
                val localWatchedUpdatedAt = localShowsSyncSource.getWatchedUpdatedAt()
                val localEpisodeHistoryUpdatedAt = localEpisodesSyncSource.getHistoryUpdatedAt()

                if (localWatchlistUpdatedAt == null &&
                    localWatchedUpdatedAt == null &&
                    localEpisodeHistoryUpdatedAt == null
                ) {
                    return@launch
                }

                if (localWatchlistUpdatedAt?.isAfter(loadedAt) == true ||
                    localWatchedUpdatedAt?.isAfter(loadedAt) == true ||
                    localEpisodeHistoryUpdatedAt?.isAfter(loadedAt) == true
                ) {
                    loadData(showLoading = false)
                    Timber.d("Updating up next shows")
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Error")
                }
            }
        }
    }

    val state: StateFlow<HomeUpNextState> = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        HomeUpNextState(
            isLoading = s1,
            items = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
