package tv.trakt.trakt.tv.core.home.sections.shows.upcoming

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.tv.core.home.sections.shows.upcoming.usecases.GetUpcomingUseCase
import tv.trakt.trakt.tv.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.trakt.tv.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.tv.helpers.extensions.nowUtc
import tv.trakt.trakt.tv.helpers.extensions.rethrowCancellation
import java.time.ZonedDateTime

internal class HomeUpcomingViewModel(
    private val getUpcomingUseCase: GetUpcomingUseCase,
    private val localShowsSyncSource: ShowsSyncLocalDataSource,
    private val localEpisodesSyncSource: EpisodesSyncLocalDataSource,
) : ViewModel() {
    private val initialState = HomeUpcomingState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadedAt: ZonedDateTime? = null

    init {
        loadData()
    }

    private fun loadData(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update { true }
                }

                val items = getUpcomingUseCase.getCalendar()
                itemsState.update { items }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Log.e("HomeUpcomingViewModel", "Failed to load data", error)
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun updateData() {
        Log.d("HomeUpcomingViewModel", "updateData called")
        viewModelScope.launch {
            try {
                if (loadedAt == null) {
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
                    Log.d("HomeUpcomingViewModel", "Updating upcoming shows")
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Log.e("HomeUpcomingViewModel", "Error", error)
                }
            }
        }
    }

    val state: StateFlow<HomeUpcomingState> = combine(
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
