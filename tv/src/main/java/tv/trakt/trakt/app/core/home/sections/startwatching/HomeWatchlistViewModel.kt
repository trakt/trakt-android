package tv.trakt.trakt.app.core.home.sections.startwatching

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
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_WATCHLIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem
import tv.trakt.trakt.app.core.home.sections.startwatching.usecases.GetHomeMoviesWatchlistItemsUseCase
import tv.trakt.trakt.app.core.home.sections.startwatching.usecases.GetHomeShowsWatchlistItemsUseCase
import tv.trakt.trakt.app.core.scrobble.data.local.ScrobbleUpdates
import tv.trakt.trakt.app.core.scrobble.data.local.ScrobbleUpdates.Source.SCROBBLE_STOP_WORKER
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import tv.trakt.trakt.common.model.pagination.Pagination
import java.time.ZonedDateTime

internal class HomeWatchlistViewModel(
    private val getShowsUseCase: GetHomeShowsWatchlistItemsUseCase,
    private val getMoviesUseCase: GetHomeMoviesWatchlistItemsUseCase,
    private val localMoviesSyncSource: MoviesSyncLocalDataSource,
    private val localShowsSyncSource: ShowsSyncLocalDataSource,
    private val scrobbleUpdates: ScrobbleUpdates,
    private val appLifecycleProvider: AppLifecycleProvider,
) : ViewModel() {
    private val initialState = HomeWatchlistState()

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
                    val showsAsync = async { getShowsUseCase.getItems(Pagination(1, HOME_WATCHLIST_PAGE_LIMIT)) }
                    val moviesAsync = async { getMoviesUseCase.getItems(Pagination(1, HOME_WATCHLIST_PAGE_LIMIT)) }
                    awaitAll(showsAsync, moviesAsync)
                }
                    .flatten()
                    .distinctBy { it.key }
                    .sortedWith(
                        compareByDescending<WatchlistItem> { it.released }
                            .thenByDescending { it.listedAt },
                    )
                    .take(HOME_SECTION_LIMIT)
                    .also { items ->
                        itemsState.update {
                            items.toImmutableList()
                        }
                    }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Error loading watchlist movies")
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
                val localShowsUpdatedAt = localShowsSyncSource.getWatchlistUpdatedAt()
                val localMoviesUpdatedAt = localMoviesSyncSource.getWatchlistUpdatedAt()

                if (
                    (localShowsUpdatedAt != null && loadedAt?.isBefore(localShowsUpdatedAt) == true) ||
                    (localMoviesUpdatedAt != null && loadedAt?.isBefore(localMoviesUpdatedAt) == true)
                ) {
                    loadData(showLoading = false)
                    Timber.d("Updating watchlist movies")
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Error")
                }
            }
        }
    }

    val state: StateFlow<HomeWatchlistState> = combine(
        itemsState,
        loadingState,
        errorState,
    ) { s1, s2, s3 ->
        HomeWatchlistState(
            items = s1,
            isLoading = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
