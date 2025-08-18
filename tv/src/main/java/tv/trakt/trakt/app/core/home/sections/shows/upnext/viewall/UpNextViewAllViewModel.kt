package tv.trakt.trakt.app.core.home.sections.shows.upnext.viewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_PAGE_LIMIT
import tv.trakt.trakt.app.core.home.sections.shows.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.app.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import java.time.ZonedDateTime

internal class UpNextViewAllViewModel(
    private val getUpNextUseCase: GetUpNextUseCase,
    private val localShowsSyncSource: ShowsSyncLocalDataSource,
    private val localEpisodesSyncSource: EpisodesSyncLocalDataSource,
) : ViewModel() {
    private val initialState = UpNextViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

    private var nextDataPage: Int = 1
    private var hasMoreData: Boolean = true

    private var loadedAt: ZonedDateTime? = null

    init {
        loadData()
    }

    private fun loadData(showLoading: Boolean = true) {
        if (loadingState.value || loadingPageState.value) {
            return
        }
        viewModelScope.launch {
            try {
                nextDataPage = 1
                itemsState.update { null }
                if (showLoading) {
                    loadingState.update { true }
                }

                val items = getUpNextUseCase.getUpNext(
                    limit = HOME_PAGE_LIMIT,
                    page = 1,
                )
                itemsState.update { items }

                nextDataPage += 1
                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Failed to load data")
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun loadNextDataPage() {
        if (loadingPageState.value || !hasMoreData) {
            return
        }
        viewModelScope.launch {
            try {
                loadingPageState.update { true }

                val items = getUpNextUseCase.getUpNext(
                    limit = HOME_PAGE_LIMIT,
                    page = nextDataPage,
                )

                itemsState.update {
                    it?.toPersistentList()?.plus(items)
                }

                hasMoreData = (items.size >= HOME_PAGE_LIMIT)
                nextDataPage += 1
                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingPageState.update { false }
            }
        }
    }

    fun updateData() {
        Timber.d("updateData called")
        viewModelScope.launch {
            try {
                if (loadedAt == null) {
                    // No data loaded yet, nothing to update
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
        loadingPageState,
        itemsState,
        errorState,
    ) { s1, s2, s3, s4 ->
        UpNextViewAllState(
            isLoading = s1,
            isLoadingPage = s2,
            items = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
