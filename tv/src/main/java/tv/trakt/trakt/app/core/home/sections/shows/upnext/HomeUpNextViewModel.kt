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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.Config.REFRESH_DATA_THRESHOLD_MINUTES
import tv.trakt.trakt.app.core.home.sections.shows.upnext.usecases.GetMoviesUpNextUseCase
import tv.trakt.trakt.app.core.home.sections.shows.upnext.usecases.GetShowsUpNextUseCase
import tv.trakt.trakt.app.core.scrobble.data.local.ScrobbleUpdates
import tv.trakt.trakt.app.core.scrobble.data.local.ScrobbleUpdates.Source.SCROBBLE_STOP_WORKER
import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import java.time.ZonedDateTime

internal class HomeUpNextViewModel(
    private val getShowsUpNextUseCase: GetShowsUpNextUseCase,
    private val getMoviesUpNextUseCase: GetMoviesUpNextUseCase,
    private val localShowsSyncSource: ShowsSyncLocalDataSource,
    private val localEpisodesSyncSource: EpisodesSyncLocalDataSource,
    private val scrobbleUpdates: ScrobbleUpdates,
    private val appLifecycleProvider: AppLifecycleProvider,
) : ViewModel() {
    private val initialState = HomeUpNextState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadedAt: ZonedDateTime? = null
    private var dataJob: Job? = null

    init {
        loadData()
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
        merge(
            scrobbleUpdates.observeUpdates(SCROBBLE_STOP_WORKER),
        )
            .distinctUntilChanged()
            .debounce(250)
            .onEach {
                loadData(showLoading = false)
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
                    val showsAsync = async { getShowsUpNextUseCase.getShowsUpNext() }
                    val moviesAsync = async { getMoviesUpNextUseCase.getMoviesUpNext() }

                    val items = awaitAll(showsAsync, moviesAsync)
                        .flatten()
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
