package tv.trakt.trakt.app.core.home.sections.startwatching.viewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_WATCHLIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem
import tv.trakt.trakt.app.core.home.sections.startwatching.usecases.GetHomeMoviesWatchlistItemsUseCase
import tv.trakt.trakt.app.core.home.sections.startwatching.usecases.GetHomeShowsWatchlistItemsUseCase
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.pagination.Pagination
import java.time.ZonedDateTime

internal class WatchlistViewAllViewModel(
    private val getShowsUseCase: GetHomeShowsWatchlistItemsUseCase,
    private val getMoviesUseCase: GetHomeMoviesWatchlistItemsUseCase,
    private val localMoviesDataSource: MoviesSyncLocalDataSource,
    private val localShowsDataSource: ShowsSyncLocalDataSource,
) : ViewModel() {
    private val initialState = WatchlistViewAllState()

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
                    .also { items ->
                        itemsState.update {
                            items.toImmutableList()
                        }
                    }

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

                val items = coroutineScope {
                    val showsAsync =
                        async { getShowsUseCase.getItems(Pagination(nextDataPage, HOME_WATCHLIST_PAGE_LIMIT)) }
                    val moviesAsync =
                        async { getMoviesUseCase.getItems(Pagination(nextDataPage, HOME_WATCHLIST_PAGE_LIMIT)) }
                    awaitAll(showsAsync, moviesAsync)
                }

                itemsState.update { state ->
                    state
                        ?.plus(items.flatten())
                        ?.distinctBy { it.key }
                        ?.sortedWith(
                            compareByDescending<WatchlistItem> { it.released }
                                .thenByDescending { it.listedAt },
                        )
                        ?.toPersistentList()
                }

                hasMoreData = (items.size >= HOME_WATCHLIST_PAGE_LIMIT)
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
                val localShowsUpdatedAt = localShowsDataSource.getWatchlistUpdatedAt()
                val localMoviesUpdatedAt = localMoviesDataSource.getWatchlistUpdatedAt()

                if (
                    (localShowsUpdatedAt != null && loadedAt?.isBefore(localShowsUpdatedAt) == true) ||
                    (localMoviesUpdatedAt != null && loadedAt?.isBefore(localMoviesUpdatedAt) == true)
                ) {
                    loadData()
                    Timber.d("Updating watchlist movies")
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
        WatchlistViewAllState(
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
