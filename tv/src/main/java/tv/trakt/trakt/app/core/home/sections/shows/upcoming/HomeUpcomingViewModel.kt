package tv.trakt.trakt.app.core.home.sections.shows.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.Config.REFRESH_DATA_THRESHOLD_MINUTES
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.usecases.GetUpcomingUseCase
import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import java.time.ZonedDateTime

internal class HomeUpcomingViewModel(
    private val getUpcomingUseCase: GetUpcomingUseCase,
    private val localShowsSyncSource: ShowsSyncLocalDataSource,
    private val localMoviesSyncSource: MoviesSyncLocalDataSource,
    private val localEpisodesSyncSource: EpisodesSyncLocalDataSource,
    private val appLifecycleProvider: AppLifecycleProvider,
) : ViewModel() {
    private val initialState = HomeUpcomingState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadedAt: ZonedDateTime? = null

    init {
        loadData()
        observeApp()
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

    private fun loadData(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update { true }
                }

                val items = getUpcomingUseCase.getUpcoming()
                itemsState.update { items }

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
                    return@launch
                }

                val showsWatchlistUpdatedAt = localShowsSyncSource.getWatchlistUpdatedAt()
                val showsWatchedUpdatedAt = localShowsSyncSource.getWatchedUpdatedAt()

                val moviesWatchlistUpdatedAt = localMoviesSyncSource.getWatchlistUpdatedAt()
                val moviesWatchedUpdatedAt = localMoviesSyncSource.getWatchedUpdatedAt()

                val localEpisodeHistoryUpdatedAt = localEpisodesSyncSource.getHistoryUpdatedAt()

                if (showsWatchlistUpdatedAt == null &&
                    showsWatchedUpdatedAt == null &&
                    moviesWatchlistUpdatedAt == null &&
                    moviesWatchedUpdatedAt == null &&
                    localEpisodeHistoryUpdatedAt == null
                ) {
                    return@launch
                }

                if (showsWatchlistUpdatedAt?.isAfter(loadedAt) == true ||
                    showsWatchedUpdatedAt?.isAfter(loadedAt) == true ||
                    moviesWatchlistUpdatedAt?.isAfter(loadedAt) == true ||
                    moviesWatchedUpdatedAt?.isAfter(loadedAt) == true ||
                    localEpisodeHistoryUpdatedAt?.isAfter(loadedAt) == true
                ) {
                    loadData(showLoading = false)
                    Timber.d("Updating upcoming items.")
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Error")
                }
            }
        }
    }

    val state = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        HomeUpcomingState(
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
